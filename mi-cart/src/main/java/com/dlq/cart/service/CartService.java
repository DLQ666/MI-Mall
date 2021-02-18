package com.dlq.cart.service;

import com.dlq.cart.vo.Cart;
import com.dlq.cart.vo.CartItem;

import java.util.List;
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

    /**
     * 获取整个购物车
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车数据
     * @param cartkey
     */
    void clearCart(String cartkey);

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 全选
     * @param skuIds
     */
    void checkAllItem(List<Long> skuIds,Integer check);

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     */
    void countItem(Long skuId, Integer num);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteItem(Long skuId);
}
