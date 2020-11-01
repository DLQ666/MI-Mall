/**
  * Copyright 2020 bejson.com 
  */
package com.dlq.mall.product.vo.spuvo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2020-10-30 22:2:5
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {

    //spu基本信息 pms_spu_info
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;

    //spu的描述图片 pms_spu_info_desc
    private List<String> decript;

    //spu的图片集 pms_spu_images
    private List<String> images;

    //spu的积分信息mi_sms-》sms_spu_bounds
    private Bounds bounds;

    //spu的规格参数 pms_product_attr_value
    private List<BaseAttrs> baseAttrs;

    //5、当前spu对应的所有sku信息
    private List<Skus> skus;

}
