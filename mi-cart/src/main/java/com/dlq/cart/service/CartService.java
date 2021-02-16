package com.dlq.cart.service;

import com.dlq.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-02-11 12:26
 */
public interface CartService {

    /**
     * 将商品添加到购物车
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车中某个购物项
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);
}
