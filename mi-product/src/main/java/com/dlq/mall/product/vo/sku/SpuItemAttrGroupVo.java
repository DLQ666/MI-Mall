package com.dlq.mall.product.vo.sku;

import com.dlq.mall.product.vo.spuvo.Attr;
import lombok.Data;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-03 15:23
 */
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
