package com.dlq.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.enums.OrderStatusEnum;
import com.dlq.common.exception.NoStockException;
import com.dlq.common.to.mq.OrderTo;
import com.dlq.common.to.mq.StockDetailTo;
import com.dlq.common.to.mq.StockLockedTo;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;
import com.dlq.common.utils.R;
import com.dlq.mall.ware.dao.WareSkuDao;
import com.dlq.mall.ware.entity.WareOrderTaskDetailEntity;
import com.dlq.mall.ware.entity.WareOrderTaskEntity;
import com.dlq.mall.ware.entity.WareSkuEntity;
import com.dlq.mall.ware.feign.OrderFeignService;
import com.dlq.mall.ware.feign.ProductFeignService;
import com.dlq.mall.ware.service.WareOrderTaskDetailService;
import com.dlq.mall.ware.service.WareOrderTaskService;
import com.dlq.mall.ware.service.WareSkuService;
import com.dlq.mall.ware.vo.OrderItemVo;
import com.dlq.mall.ware.vo.OrderVo;
import com.dlq.mall.ware.vo.SkuHasStockVo;
import com.dlq.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    OrderFeignService orderFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //skuId: 1
        //wareId: 2
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断还没有这个记录 新增操作
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            //TODO 远程调用商品服务获取sku名字,失败事务无需回滚
            //1、自己catch异常
            // / ToDo 还可以用什么办法让异常出现以后不回滚?高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            //有这个记录 更新库存操作
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {

        return skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //查询当前sku总库存量
            //SELECT SUM(stock-stock_locked) FROM `ims_ware_sku` WHERE sku_id=1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            //vo.setHasStock(count == null ? false : count > 0);
            vo.setHasStock(count != null && count > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 库存自动解锁
     * 1、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁。
     * 2、订单失败。
     *    锁库存失败导致
     *
     *  只要解锁库存的消息失败。一定要告诉服务器解锁失败。
     */
    @Override
    public void unLockStock(StockLockedTo to) {

        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //解锁
        //1、查询数据库关于这个订单的锁定库存信息
        //   如果查到了；有：证明库存锁定成功了
        //      解锁；订单情况。
        //              1、没有这个订单。就必须解锁
        //              2、有这个订单。不能直接解锁，得判断
        //                      订单状态：已取消：解锁库存
        //                               没取消；不能解锁
        //   没查到；没有：库存锁定失败了，库存回滚了。这种情况无需解锁，都回滚了 解锁个锤子
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null){
            //解锁
            //先查到库存工作单的id--查出工作单是哪个订单，再去订单库查询订单状态
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            //根据订单号查询订单的状态
            String orderSn = taskEntity.getOrderSn();

            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0){
                //订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {});
                if (data == null || OrderStatusEnum.CANCLED.getCode().equals(data.getStatus())){
                    //订单不存在  或者  订单已经被取消 。才能解锁库存

                    if (byId.getLockStatus() == 1){
                        //当前库存工作单详情，状态1 已锁定但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum(),detailId);
                    }
                }
            }else {
                //消息拒绝以后重新放到队列里面，让别人继续消费解锁。
                throw new RuntimeException("远程服务失败...");
            }
        }else {
            //无需解锁，都回滚了 解锁个锤子
        }
    }

    //防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期。查订单状态肯定为新建状态，什么都不做就走了
    //导致卡顿的订单，永远不能解锁库存
    @Transactional
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查一下最新库存的状态，防止重复解锁库存
        WareOrderTaskEntity orderTaskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = orderTaskEntity.getId();
        //按照工作单找到所有 没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", id)
                        .eq("lock_status", 1));
        if (list == null && list.size()==0){
            return;
        }
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }

    private void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        //库存解锁
        wareSkuDao.unLockStock(skuId,wareId,num,taskDetailId);
        //更新库存工作单状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        //修改为已解锁
        entity.setLockStatus(2);
        orderTaskDetailService.updateById(entity);
    }

    /**
     * 为某个订单锁定库存
     * (rollbackFor = NoStockException.class)--》标不标都可以
     * 因为默认只要是运行时异常都会回滚
     * @param vo
     *
     * 库存解锁场景：
     * 1）、下订单成功，订单过期没有支付被系统自动取消，被用户手动取消。都要解锁库存。
     *
     * 2）、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     *      之前锁定的库存就要自动解锁。
     *
     */
    @Transactional//(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存库存工作单的详情。
         * 追溯
         */
        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity();
        orderTaskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(orderTaskEntity);

        //按照下单的收货地址，找到一个就近仓库，锁定库存。
        //1、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        //2、锁定库存
        for (SkuWareHasStock skuWareHasStock : collect) {
            boolean skuStocked = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有任何仓库有这个商品库存
                throw new NoStockException(skuId);
            }

            //如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            //如果锁定失败。前面保存的工作单信息就回滚了。发送出去的消息，即使要解锁记录，由于去数据库查不到id，所以就不用解锁
            //  1：1-2-3  2：2-1-2  3：3-2-3（x）
            //循环遍历查询每个仓库库存是否足够--足够就锁库存---否则--锁库存失败抛异常
            for (Long wareId : wareIds) {
                //成功就返回1，否则就返回0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,skuWareHasStock.getNum());
                if (count == 1){
                    //锁定库存成功 -- 也就是修改了锁库存字段的值
                    skuStocked = true;
                    // TODO 发送消息---告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null,skuId,null,skuWareHasStock.getNum(), orderTaskEntity.getId(), wareId,1);
                    orderTaskDetailService.save(detailEntity);
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, stockDetailTo);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    //只发id，不行，，防止回滚以后找不到数据。
                    stockLockedTo.setId(orderTaskEntity.getId());
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked" , stockLockedTo );
                    //然后终止循环锁定下一个仓库的库存
                    break;
                } //当前仓库锁失败。重试下一个仓库

            }
            if (!skuStocked){
                //说明当前商品的所有仓库库存都不足，没有锁住库存
                //那么抛库存不足异常
                throw new NoStockException(skuId);
            }
        }

        //3、能走到这，肯定说明商品都是有库存的，并且都是锁定成功的
        return true;
    }

    @Override
    public SkuHasStockVo getSkuIsStock(Long skuId) {
        SkuHasStockVo vo = new SkuHasStockVo();
        Long count = baseMapper.getSkuStock(skuId);
        vo.setSkuId(skuId);
        //vo.setHasStock(count == null ? false : count > 0);
        vo.setHasStock(count != null && count > 0);
        return vo;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}
