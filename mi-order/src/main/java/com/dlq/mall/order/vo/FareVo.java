package com.dlq.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-23 15:03
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
