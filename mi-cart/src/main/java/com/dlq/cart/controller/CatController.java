package com.dlq.cart.controller;

import com.dlq.cart.interceptor.CartInterceptor;
import com.dlq.cart.vo.UserInfoTo;
import com.dlq.common.constant.AuthServerConstant;
import com.sun.media.sound.SoftTuning;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-11 18:38
 */
@Controller
public class CatController {

    /**
     * 浏览器有一个cookie；user-key；标识用户身份，一个月后过期；
     * 如果第一次使用jd的购物车，都会给一个临时的用户身份；
     * 浏览器以后保存，每次访问都会带上这个cookie
     *
     * 登录：session有
     * 没登录：按照cookie里面带来的user-key来做
     *
     * 第一次使用：如果没有临时用户，帮忙创建一个临时用户
     *
     * 如何做？？？？？？？？采用 拦截器 + ThreadLocal
     * 目标方法放行之前先让拦截器获取用户的登录或者没登录的状态信息，如果用户没登录而且cookie中还没有临时用户，还可以创建一个临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(){

        //1、快速得到用户信息，id，user-key
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo);

        return "cartList";
    }
}
