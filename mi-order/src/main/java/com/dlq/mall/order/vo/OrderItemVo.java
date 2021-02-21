package com.dlq.mall.order.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 *@program: MI-Mall
 *@description: 所有选中的购物项
 *@author: Hasee
 *@create: 2021-02-21 20:48
 */
public class OrderItemVo {

    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
}
