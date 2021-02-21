package com.dlq.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 19:54:25
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询会员的收货地址列表
     * @param memberId 会员id
     * @return 地址列表
     */
    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

