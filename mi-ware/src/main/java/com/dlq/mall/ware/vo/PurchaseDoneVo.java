package com.dlq.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-05 15:22
 */
@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id; //采购单id

    private List<PurchaseItemDoneVo> items;
}
