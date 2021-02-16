package com.dlq.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dlq.cart.feign.ProductFeignService;
import com.dlq.cart.interceptor.CartInterceptor;
import com.dlq.cart.service.CartService;
import com.dlq.cart.vo.CartItem;
import com.dlq.cart.vo.SkuInfoVo;
import com.dlq.cart.vo.UserInfoTo;
import com.dlq.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-11 12:26
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;

    private final String CART_PREFIX = "dlq-mall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        //判断购物车是否有此商品
        String res = (String) cartOps.get(skuId.toString());
        //如果没有--新添加
        if (StringUtils.isEmpty(res)) {
            //购物车无此商品
            //添加新商品到购物车
            //1、远程查询当前要添加的商品的信息
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfoData = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                //添加商品信息
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfoData.getSkuDefaultImg());
                cartItem.setTitle(skuInfoData.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfoData.getPrice());
            }, executor);

            //2、远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                //添加商品sku的组合信息
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);
            return cartItem;
        } else {
            //购物车有此商品
            //如果有--修改数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);

            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String str = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(str, CartItem.class);
    }

    /**
     * 获取到我们要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //快速得到用户信息，id，user-key
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1、判断用户是否登录==
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            //--如果登录--userInfoTo.getUserId() 不为空 cartKey = dlq-mall:cart:userId(4)
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            //临时购物车
            //--如果没登录--userInfoTo.getUserId() 为空 cartKey = dlq-mall:cart:userKey(uuid)
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
