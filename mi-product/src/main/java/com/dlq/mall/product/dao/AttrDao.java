package com.dlq.mall.product.dao;

import com.dlq.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchAttrIds(@Param("attrsIds") List<Long> attrsIds);
}
