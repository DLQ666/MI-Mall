package com.dlq.mall.ware.vo;

import lombok.Data;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-23 17:11
 */
@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private boolean locked;
}
