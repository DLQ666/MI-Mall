package com.dlq.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dlq.auth.feign.MemberFeignService;
import com.dlq.auth.util.WeiboProperties;
import com.dlq.auth.vo.MemberRespVo;
import com.dlq.auth.vo.SocialUser;
import com.dlq.common.utils.HttpClientUtils;
import com.dlq.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

/**
 *@program: MI-Mall
 *@description: 处理社交登录请求
 *@author: Hasee
 *@create: 2021-02-11 21:52
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private WeiboProperties weiboProperties;
    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 社交登陆成功回调
     * @param code
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws IOException, ParseException {

        //1、根据code换取accessToken；
        //https://api.weibo.com/oauth2/access_token?
        // client_id=YOUR_CLIENT_ID&
        // client_secret=YOUR_CLIENT_SECRET&
        // grant_type=authorization_code&
        // redirect_uri=YOUR_REGISTERED_REDIRECT_URI&
        // code=CODE
        String accessTokenUrl = "https://api.weibo.com/oauth2/access_token";
        HashMap<String, String> param = new HashMap<>();
        param.put("client_id", weiboProperties.getClientId());
        param.put("client_secret", weiboProperties.getClientSecret());
        param.put("grant_type", weiboProperties.getGrantType());
        param.put("redirect_uri", weiboProperties.getRedirectUri());
        param.put("code", code);
        HttpClientUtils client = new HttpClientUtils(accessTokenUrl, param);
        //发送请求：组装完整的url字符串、发送请求
        client.post();
        if (client.getStatusCode() == 200){
            //获取到了access_token
            //得到响应
            String result = client.getContent();
            log.info("result = " + result);
            SocialUser socialUser = JSON.parseObject(result, SocialUser.class);

            //知道了当前是哪个社交用户登录
            //1）当前用户如果是第一次进网站，自动注册进来(为当前社交用户自动注册一个会员账号，以后这个社交账号就对应指定的会员)
            //登录或者注册这个社交用户
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0){
                MemberRespVo data = oauthLogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                System.out.println("登录成功：用户信息=="+data);
                log.info("登录成功：用户：{}", data.toString());
                //2、登录成功就跳回首页
                return "redirect:http://dlqk8s.top:81";
            }else {
                return "redirect:http://auth.dlqk8s.top:81/login.html";
            }

        }else {
            return "redirect:http://auth.dlqk8s.top:81/login.html";
        }
    }
}
