package com.dlq.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 20:12:30
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn);
}

