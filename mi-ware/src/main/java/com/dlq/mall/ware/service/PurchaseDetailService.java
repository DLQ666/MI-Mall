package com.dlq.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-11-04 15:00:15
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

