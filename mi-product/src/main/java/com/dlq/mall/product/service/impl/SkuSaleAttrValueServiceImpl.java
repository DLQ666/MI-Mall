package com.dlq.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;
import com.dlq.mall.product.dao.SkuSaleAttrValueDao;
import com.dlq.mall.product.entity.SkuSaleAttrValueEntity;
import com.dlq.mall.product.service.SkuSaleAttrValueService;
import com.dlq.mall.product.vo.sku.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        return baseMapper.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        return dao.getSkuSaleAttrValuesAsStringList(skuId);
    }

    /*@Override
    public List<SkuItemSaleAttrVo> getSaleAttrsVersionBySpuId(Long spuId) {
        return baseMapper.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsColorBySpuId(Long spuId) {
        return baseMapper.getSaleAttrsBySpuId(spuId);
    }*/

}
