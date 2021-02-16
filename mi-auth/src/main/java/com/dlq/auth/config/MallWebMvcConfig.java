package com.dlq.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-07 10:53
 */
@Configuration
public class MallWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/regist.html").setViewName("regist");
    }
}
