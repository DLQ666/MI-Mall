package com.dlq.mall.coupon.dao;

import com.dlq.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 19:43:06
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
