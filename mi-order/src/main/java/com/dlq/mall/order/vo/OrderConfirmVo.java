package com.dlq.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *@program: MI-Mall
 *@description: 订单确认页需要用的数据
 *@author: Hasee
 *@create: 2021-02-21 20:41
 */
@Data
public class OrderConfirmVo {

    //收货地址列表 对应ums_member_receive_address表
    List<MemberAddressVo> address;

    //所有选中的购物项
    List<OrderItemVo> items;

    //发票记录....

    //京豆--积分
    Integer integration;

    //总额//订单总额
    BigDecimal total;

    //应付价格
    BigDecimal payPrice;
}
