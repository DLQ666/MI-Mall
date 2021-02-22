package com.dlq.cart.controller;

import com.dlq.cart.service.CartService;
import com.dlq.cart.vo.Cart;
import com.dlq.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-11 18:38
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getCurrentUserCartItems();
    }

    /**
     * 全选删除购物项
     */
    @GetMapping("/deleteAllItem")
    public String deleteAllItem(@RequestParam("skuIds")List<Long> skuIds){
        cartService.deleteAllItem(skuIds);
        return "redirect:http://cart.dlqk8s.top:81/cart.html";
    }

    /**
     * 删除购物项
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.dlqk8s.top:81/cart.html";
    }
    /**
     * 修改购物车数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("countItem")
    public String countItem(@RequestParam("skuId")Long skuId,@RequestParam("num")Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.dlqk8s.top:81/cart.html";
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("check")Integer check){
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.dlqk8s.top:81/cart.html";
    }

    /**
     * 全选
     * @param skuIds
     * @param check
     * @return
     */
    @GetMapping("/checkAllItem")
    public String checkAllItem(@RequestParam("skuIds")List<Long> skuIds ,@RequestParam("check")Integer check){
        cartService.checkAllItem(skuIds,check);
        return "redirect:http://cart.dlqk8s.top:81/cart.html";
    }

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
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        //1、快速得到用户信息，id，user-key
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        System.out.println(userInfoTo);
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
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
