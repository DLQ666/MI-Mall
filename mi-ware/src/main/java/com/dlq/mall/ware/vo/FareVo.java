package com.dlq.mall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-22 23:35
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
