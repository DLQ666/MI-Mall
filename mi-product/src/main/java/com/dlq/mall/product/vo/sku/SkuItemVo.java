package com.dlq.mall.product.vo.sku;

import com.dlq.mall.product.entity.SkuImagesEntity;
import com.dlq.mall.product.entity.SkuInfoEntity;
import com.dlq.mall.product.entity.SpuInfoDescEntity;
import com.dlq.mall.product.entity.SpuInfoEntity;
import lombok.Data;

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
    //2、获取sku的图片信息pms_sku_images
    SkuImagesEntity images;
    //3、获取spu的销售属性组合

    //4、获取spu的介绍
    SpuInfoDescEntity desc;
    //5、获取spu的规格参数信息


    public static class SkuItemSaleAttrVo{

    }
}
