package com.dlq.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-04 16:07
 */
@Data
public class MergeVo {
    //{
    //  purchaseId: 1, //整单id
    //  items:[1,2,3,4] //合并项集合
    //}

    private Long purchaseId;//整单id
    private List<Long> items;//[1,2,3,4] //合并项集合
}
