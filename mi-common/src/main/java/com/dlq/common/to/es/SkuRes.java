package com.dlq.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-02 14:29
 */
@Data
public class SkuRes {

    private String skuTitle;
    private String skuImg;
    private BigDecimal skuPrice;
    private Long spuId;
    private Long skuId;

}
