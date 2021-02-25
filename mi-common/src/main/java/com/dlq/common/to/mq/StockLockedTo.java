package com.dlq.common.to.mq;

import lombok.Data;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-25 14:16
 */
@Data
public class StockLockedTo {
    private Long id; //库存工作单id
    private StockDetailTo detail;
}
