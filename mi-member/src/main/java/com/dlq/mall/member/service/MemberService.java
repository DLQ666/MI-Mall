package com.dlq.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.member.entity.MemberEntity;
import com.dlq.mall.member.vo.MemLoginVo;
import com.dlq.mall.member.vo.MemRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 19:54:25
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemRegistVo registVo);

    void checkPhoneUnique(String phone);

    void checkUsernameUnique(String username);

    MemberEntity login(MemLoginVo loginVo);
}

