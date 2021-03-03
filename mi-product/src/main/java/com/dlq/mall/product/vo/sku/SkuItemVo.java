package com.dlq.mall.product.vo.sku;

import com.dlq.mall.product.entity.SkuImagesEntity;
import com.dlq.mall.product.entity.SkuInfoEntity;
import com.dlq.mall.product.entity.SpuInfoDescEntity;
import com.dlq.mall.product.vo.seckillvo.SeckillInfoVo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-01-30 15:53
 */
@Data
public class SkuItemVo {

    //1、sku基本信息获取  pms_sku_info
    SkuInfoEntity info;

    boolean hasStock;

    //2、获取sku的图片信息pms_sku_images
    List<SkuImagesEntity> images;
    //3、获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttrs;
    //4、获取spu的介绍
    SpuInfoDescEntity desc;
    //5、获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    //当前商品的秒杀优惠信息
    SeckillInfoVo seckillInfoVo;

}
