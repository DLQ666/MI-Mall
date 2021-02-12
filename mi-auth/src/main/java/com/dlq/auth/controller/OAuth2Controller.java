package com.dlq.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *@program: MI-Mall
 *@description: 处理社交登录请求
 *@author: Hasee
 *@create: 2021-02-11 21:52
 */
@Controller
public class OAuth2Controller {

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code){

        //1、根据code换取accessToken；

        //2、登录成功就跳回首页
        return "";
    }
}
