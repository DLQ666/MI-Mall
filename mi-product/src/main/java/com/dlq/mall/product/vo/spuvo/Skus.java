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
public class Skus {

    //sku的销售属性信息 pms_sku_sale_attr_value
    private List<Attr> attr;

    //sku的基本信息 pms_sku_info
    private String skuName;
    private BigDecimal price;
    private String skuTitle;
    private String skuSubtitle;

    //sku的图片信息 pms_sku_images
    private List<Images> images;

    private List<String> descar;

    //sku的优惠、满减等信息 跨库操作mi_sms-》sms_sku_ladder\sms_sku_full_reduction\sms_member_price\
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;

}
