package com.dlq.mall.order.to;

import com.dlq.mall.order.entity.OrderEntity;
import com.dlq.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-23 14:53
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;//订单应付金额

    private BigDecimal fare;//运费
}
