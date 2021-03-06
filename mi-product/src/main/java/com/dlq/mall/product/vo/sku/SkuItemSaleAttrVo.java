package com.dlq.mall.product.vo.sku;

import lombok.Data;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-04 17:44
 */
@Data
public class SkuItemSaleAttrVo {
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
