package com.dlq.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.to.mq.OrderTo;
import com.dlq.common.to.mq.StockLockedTo;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.ware.entity.WareSkuEntity;
import com.dlq.mall.ware.vo.LockStockResult;
import com.dlq.mall.ware.vo.SkuHasStockVo;
import com.dlq.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 20:12:30
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     * 查询某个sku是否有库存
     * @param skuId
     * @return
     */
    SkuHasStockVo getSkuIsStock(Long skuId);

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo orderTo);
}

