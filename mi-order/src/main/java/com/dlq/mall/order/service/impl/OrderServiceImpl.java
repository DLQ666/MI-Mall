package com.dlq.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.dlq.common.utils.R;
import com.dlq.common.vo.MemberRespVo;
import com.dlq.mall.order.feign.CartFeignService;
import com.dlq.mall.order.feign.MemberFeignService;
import com.dlq.mall.order.feign.WareFeignService;
import com.dlq.mall.order.interceptor.LoginUserInterceptor;
import com.dlq.mall.order.vo.MemberAddressVo;
import com.dlq.mall.order.vo.OrderConfirmVo;
import com.dlq.mall.order.vo.OrderItemVo;
import com.dlq.mall.order.vo.SkuStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.order.dao.OrderDao;
import com.dlq.mall.order.entity.OrderEntity;
import com.dlq.mall.order.service.OrderService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WareFeignService wareFeignService;

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
            orderConfirmVo.setItems(currentUserCartItems);
            //feign在远程调用之前要构造请求，调用很多的拦截器
            //RequestInterceptor interceptor : requestInterceptors
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = orderConfirmVo.getItems();
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

        CompletableFuture.allOf(getAddressFuture,getCartItemsFuture).get();

        return orderConfirmVo;
    }

}
