package com.dlq.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-10-31 15:15
 */
@Data
public class SpuBoundsTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}
