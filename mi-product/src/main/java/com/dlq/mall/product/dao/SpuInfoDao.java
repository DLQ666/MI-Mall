package com.dlq.mall.product.dao;

import com.dlq.common.constant.ProductConstant;
import com.dlq.mall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Long spuId,@Param("code") int code);
}
