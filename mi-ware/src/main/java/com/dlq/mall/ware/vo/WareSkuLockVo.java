package com.dlq.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-23 17:06
 */
@Data
public class WareSkuLockVo {

    //订单号
    private String orderSn;

    //订单项数据  需要锁住的所有库存信息
    private List<OrderItemVo> locks;
}
