package com.dlq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.product.entity.AttrAttrgroupRelationEntity;
import com.dlq.mall.product.vo.AttrGrooupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addBatchRelation(List<AttrGrooupRelationVo> relationVo);
}

