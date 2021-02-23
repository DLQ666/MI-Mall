package com.dlq.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.exception.NoStockException;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;
import com.dlq.common.utils.R;
import com.dlq.common.vo.MemberRespVo;
import com.dlq.mall.order.constant.OrderConstant;
import com.dlq.mall.order.dao.OrderDao;
import com.dlq.mall.order.dao.OrderItemDao;
import com.dlq.mall.order.entity.OrderEntity;
import com.dlq.mall.order.entity.OrderItemEntity;
import com.dlq.mall.order.enume.OrderStatusEnum;
import com.dlq.mall.order.feign.CartFeignService;
import com.dlq.mall.order.feign.MemberFeignService;
import com.dlq.mall.order.feign.ProductFeignService;
import com.dlq.mall.order.feign.WareFeignService;
import com.dlq.mall.order.interceptor.LoginUserInterceptor;
import com.dlq.mall.order.service.OrderItemService;
import com.dlq.mall.order.service.OrderService;
import com.dlq.mall.order.to.OrderCreateTo;
import com.dlq.mall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo>  submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocalLoginUser.get();

        //ThreadLocal 是同一个线程共享数据  ---- 异步情况下不是同一个线程 获取不到attributes  到拦截器MallFeignConfig就会空指针
        //所以需要给每一个异步线程设置当前请求的 attributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(attributes);
            //1、远程查询所有的收货地址列表
            List<MemberAddressVo> addressEntities = memberFeignService.getAddressEntities(memberRespVo.getId());
            orderConfirmVo.setAddress(addressEntities);
        }, executor);

        CompletableFuture<Void> getCartItemsFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(attributes);
            //2、远程查询购物车所有选中的购物项
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            if (currentUserCartItems == null){
                return;
            }
            orderConfirmVo.setItems(currentUserCartItems);
            //feign在远程调用之前要构造请求，调用很多的拦截器
            //RequestInterceptor interceptor : requestInterceptors
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = orderConfirmVo.getItems();
            if (items == null){
                return;
            }
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            //远程查询库存系统
            R skuHasStock = wareFeignService.getSkuHasStock(skuIds);
            List<SkuStockVo> data = skuHasStock.getData("data", new TypeReference<List<SkuStockVo>>(){});
            if (data !=null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setHasStock(map);
            }
        }, executor);

        //3、查询用户积分
        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        //4、其他数据自动计算

        //5、TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");

        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(),token,30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture,getCartItemsFuture).get();

        return orderConfirmVo;
    }

    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocalLoginUser.get();
        responseVo.setCode(0);
        submitVoThreadLocal.set(vo);
        //1、验证令牌【令牌的对比和删除必须保证原子性】
        //返回0-令牌失败  1-删除成功
        String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        //以下代码为原子删除令牌操作
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);
        if (result == 0L){
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }else {
            //令牌验证成功
            //下单：创建订单，验令牌、验价格、锁库存.....
            //创建订单，订单项等信息
            OrderCreateTo order= createOrder();

            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            //验价
            if (Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){
                //金额对比成功

                //3、保存订单
                saveOrder(order);
                //4、库存锁定。只要有异常回滚订单数据。
                // 订单号，所有订单项信息（skuid，skuname，num）
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //TODO 远程锁库存
                R r = wareFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0){
                    //锁成功了
                    responseVo.setOrder(order.getOrder());
                    return responseVo;
                }else {
                    //锁失败了
                    String msg = (String) r.get("msg");
                    responseVo.setCode(3);
                    throw new NoStockException(msg);
                }

            }else {
                //验价失败
                responseVo.setCode(2);
                return responseVo;
            }
        }

        /*=======//下列代码不能保证--对比和删除是一个原子性操作--如果两次提交间隔非常短，还是会重复提交
        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
        if (orderToken!=null && orderToken.equals(redisToken)){
            //令牌通过
            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
            //执行业务
        }else {
            //不通过
        }*/
    }

    /**
     * 保存订单数据
     * @param order 订单
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 生成订单
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //1、生成订单号
        String timeId = IdWorker.getTimeId();
        //2、创建订单号
        OrderEntity orderEntity = buildOrder(timeId);
        orderCreateTo.setOrder(orderEntity);

        //2、 远程查询获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(timeId);
        orderCreateTo.setOrderItems(itemEntities);

        //3、验价  计算价格、积分等相关信息
        computePrice(orderEntity,itemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        //订单的总额，叠加每一个订单项的总额信息
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;
        for (OrderItemEntity itemEntity : itemEntities) {
            total = total.add(itemEntity.getRealAmount());
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            giftIntegration = giftIntegration + itemEntity.getGiftIntegration();
            giftGrowth = giftGrowth + itemEntity.getGiftGrowth();
        }
        //1、订单价格相关
        orderEntity.setTotalAmount(total);
        //设置应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        //设置积分等信息
        orderEntity.setIntegration(giftIntegration);
        orderEntity.setGrowth(giftGrowth);
        //设置订单删除状态0->未删除  1->已删除
        orderEntity.setDeleteStatus(0);//未删除

    }

    /**
     * 构建订单
     * @param timeId
     * @return
     */
    private OrderEntity buildOrder(String timeId) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocalLoginUser.get();
        OrderEntity entity = new OrderEntity();
        //设置会员信息
        entity.setMemberId(memberRespVo.getId());
        entity.setMemberUsername(memberRespVo.getUsername());

        entity.setCreateTime(new Date());
        //设置订单号
        entity.setOrderSn(timeId);
        //3、设置收货地址信息
        OrderSubmitVo submitVo = submitVoThreadLocal.get();
        //远程查询地址信息
        R fare = wareFeignService.getFare(submitVo.getAddrId());
        if (fare == null){
            return entity;
        }
        FareVo data = fare.getData(new TypeReference<FareVo>() {});
        if (data == null){
            return entity;
        }
        //设置运费
        entity.setFreightAmount(data.getFare());
        //设置省-市-区-详细地址
        entity.setReceiverProvince(data.getAddress().getProvince());
        entity.setReceiverCity(data.getAddress().getCity());
        entity.setReceiverRegion(data.getAddress().getRegion());
        entity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        //设置收货人-姓名-电话-邮编
        entity.setReceiverName(data.getAddress().getName());
        entity.setReceiverPhone(data.getAddress().getPhone());
        entity.setReceiverPostCode(data.getAddress().getPostCode());

        //设置订单相关的状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(15);

        return entity;
    }

    /**
     * 构建订单项数据
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String timeId) {
        //最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size()>0){
            List<OrderItemEntity> orderItemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);

                orderItemEntity.setOrderSn(timeId);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntities;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //构建每个订单项
        //1、订单信息：订单号 v
        //2、商品的SPU信息
        Long skuId = cartItem.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        if (spuInfoBySkuId == null){
            return itemEntity;
        }
        SpuInfoVo data = spuInfoBySkuId.getData("spuInfoBySkuId",new TypeReference<SpuInfoVo>() {
        });
        if (data == null){
            return itemEntity;
        }
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setCategoryId(data.getCatalogId());
        //3、商品的SKU信息 v
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        List<String> skuAttrs = cartItem.getSkuAttr();
        String skuAttr = StringUtils.collectionToDelimitedString(skuAttrs, ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4、优惠信息TODO
        //5、积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue()>>1);

        //6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额。 总额减去--各种优惠
        BigDecimal origin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = origin.subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

}
