package com.dlq.mall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *@program: MI-Mall
 *@description: 订单确认页需要用的数据
 *@author: Hasee
 *@create: 2021-02-21 20:41
 */
public class OrderConfirmVo {

    //收货地址列表 对应ums_member_receive_address表
    @Setter @Getter
    List<MemberAddressVo> address;

    //所有选中的购物项
    @Setter @Getter
    List<OrderItemVo> items;

    //发票记录....

    //优惠劵信息
    //京豆--积分
    @Setter @Getter
    Integer integration;

    //订单防重令牌，防止用户以为点击了提交订单，由于网络原因导致慢，疯狂点击
    @Setter @Getter
    String orderToken;

    //远程查询是否有库存
    @Setter @Getter
    Map<Long, Boolean> hasStock;

    //总额//订单总额
//    BigDecimal total;

    //应付价格
//    BigDecimal payPrice;

    public Integer getCount(){
        Integer i = 0;
        if (items != null){
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice(){
        return getTotal();
    }
}
