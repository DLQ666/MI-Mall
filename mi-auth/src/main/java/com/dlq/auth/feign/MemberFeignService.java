package com.dlq.auth.feign;

import com.dlq.auth.vo.UserLoginVo;
import com.dlq.auth.vo.UserRegistVo;
import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-02-08 13:44
 */
@FeignClient("mi-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo registVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo loginVo);
}
