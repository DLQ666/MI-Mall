package com.dlq.cart.controller;

import com.dlq.cart.interceptor.CartInterceptor;
import com.dlq.cart.service.CartService;
import com.dlq.cart.vo.CartItem;
import com.dlq.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-11 18:38
 */
@Controller
public class CatController {

    @Autowired
    private CartService cartService;

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
    public String cartListPage() {

        //1、快速得到用户信息，id，user-key
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num, RedirectAttributes attributes) throws ExecutionException, InterruptedException {

        if (num == 0 || num < 0) {
            num = 1;
        }
        cartService.addToCart(skuId, num);
        attributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.dlqk8s.top:81/addToCartSuccess.html";
    }

    /**
     * 跳转到购物车成功添加页面
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        //重定向到成功页面。再次查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
