package com.dlq.auth.feign;

import com.dlq.auth.vo.SocialUser;
import com.dlq.auth.vo.UserLoginVo;
import com.dlq.auth.vo.UserRegistVo;
import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.text.ParseException;

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

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser socialUser) throws IOException, ParseException;
}
