package com.dlq.mall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-22 00:03
 */
@Configuration
public class MallFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        /*return template -> {
            System.out.println("feign远程之前先进行RequestInterceptor.apply");
        };*/
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //1、RequestContextHolder拿到刚进来的这个请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null){
                    HttpServletRequest request = attributes.getRequest();//老请求
                    if (request != null){
                        //同步请求头数据，Cookie
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步了老请求的cookie
                        template.header("Cookie",cookie);
                    }
                }
            }
        };
    }
}
