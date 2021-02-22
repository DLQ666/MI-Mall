package com.dlq.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-22 20:12
 */
@Data
public class SkuStockVo {
    private Long skuId;
    private Boolean hasStock;
}
