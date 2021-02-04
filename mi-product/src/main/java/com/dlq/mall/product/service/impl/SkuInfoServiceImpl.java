package com.dlq.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;
import com.dlq.mall.product.dao.SkuInfoDao;
import com.dlq.mall.product.entity.SkuImagesEntity;
import com.dlq.mall.product.entity.SkuInfoEntity;
import com.dlq.mall.product.entity.SpuInfoDescEntity;
import com.dlq.mall.product.service.*;
import com.dlq.mall.product.vo.sku.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    /**
     * key:
     * catelogId: 0
     * brandId: 0
     * min: 0
     * max: 0
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
        return list;
    }

    @Override
    public SkuItemVo itemInfo(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1、sku基本信息获取  pms_sku_info
        SkuInfoEntity info = getById(skuId);
        Long spuId = info.getSpuId();
        skuItemVo.setInfo(info);
        //2、获取sku的图片信息pms_sku_images
        List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(images);

        //3、获取spu的销售属性组合
        ArrayList<SkuItemSaleAttrVo> list = new ArrayList<>();
        SkuItemSaleAttrVo saleAttrVersionVo = skuSaleAttrValueService.getSaleAttrsVersionBySpuId(spuId);
        SkuItemSaleAttrVo saleAttrColorVos = skuSaleAttrValueService.getSaleAttrsColorBySpuId(spuId);
        list.add(saleAttrColorVos);
        list.add(saleAttrVersionVo);
        skuItemVo.setSaleAttrs(list);
        System.out.println(skuItemVo.getSaleAttrs());
        //4、获取spu的介绍
        SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
        skuItemVo.setDesc(spuInfoDesc);

        //5、获取spu的规格参数信息
        Long catalogId = info.getCatalogId();
        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        skuItemVo.setGroupAttrs(attrGroupVos);
        return skuItemVo;
    }

}
