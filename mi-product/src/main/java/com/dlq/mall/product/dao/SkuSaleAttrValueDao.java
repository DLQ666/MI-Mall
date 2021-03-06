package com.dlq.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlq.mall.product.entity.SkuSaleAttrValueEntity;
import com.dlq.mall.product.vo.sku.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    /*SkuItemSaleAttrVo getSaleAttrsVersionBySpuId(@Param("spuId") Long spuId);

    SkuItemSaleAttrVo getSaleAttrsColorsBySpuId(@Param("spuId") Long spuId);*/

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);
}
