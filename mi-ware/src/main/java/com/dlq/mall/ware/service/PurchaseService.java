package com.dlq.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.ware.entity.PurchaseEntity;
import com.dlq.mall.ware.vo.MergeVo;
import com.dlq.mall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-11-04 15:00:15
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void receivedPurchase(List<Long> ids);

    void donePurchase(PurchaseDoneVo doneVo);
}

