package com.dlq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.product.entity.AttrEntity;
import com.dlq.mall.product.vo.AttrResponseVo;
import com.dlq.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);
}

