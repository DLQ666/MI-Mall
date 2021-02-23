package com.dlq.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-23 13:34
 */
@Data
public class OrderSubmitVo {
    private Long addrId;//收货地址的id
    private Integer payType;//支付方式
    //无需提交需要购买的商品，直接去购物车再次获取即可，京东也是这么做的
    //优惠，发票

    private String orderToken;//防重令牌
    private BigDecimal payPrice;//应付价格  验价
    private String note;//订单备注
    //用户相关信息，直接去session取出登录的用户

}
