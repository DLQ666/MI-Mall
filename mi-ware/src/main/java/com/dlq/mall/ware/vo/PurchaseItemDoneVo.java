package com.dlq.mall.ware.vo;

import lombok.Data;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-05 15:23
 */
@Data
public class PurchaseItemDoneVo {
    //{
    //		"itemId": 11,
    //		"status": 4,
    //		"reason": "无货"
    //	}
    private Long itemId;

    private Integer status;

    private String reason;
}
