package com.dlq.mall.member.dao;

import com.dlq.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 19:54:25
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
