package com.dlq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.product.entity.SpuInfoDescEntity;
import com.dlq.mall.product.entity.SpuInfoEntity;
import com.dlq.mall.product.vo.spuvo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuSaveVo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    //上架
    void up(Long spuId);

    /**
     * 根据 skuId 查询 spu 信息
     * @param skuId  skuId
     * @return spu 信息
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

