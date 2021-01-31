package com.dlq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.product.entity.SkuInfoEntity;
import com.dlq.mall.product.vo.sku.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    //根据spuid查询所有sku信息
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo itemInfo(Long skuId);
}

