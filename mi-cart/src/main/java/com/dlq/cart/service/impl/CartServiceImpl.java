package com.dlq.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dlq.cart.feign.ProductFeignService;
import com.dlq.cart.interceptor.CartInterceptor;
import com.dlq.cart.service.CartService;
import com.dlq.cart.vo.Cart;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
            cartItem.setCount(cartItem.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        if (skuId != null){
            String str = (String) cartOps.get(skuId.toString());
            return JSON.parseObject(str, CartItem.class);
        }
        return null;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        //区分登录或者没登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //1、登录 ---的购物车
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //2、如果临时购物车的数据还没有进行合并【合并购物车】
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null) {
                //临时购物车有数据，需要合并
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
                //清除临时购物车的数据
                clearCart(tempCartKey);
            }
            //3、获取登录后的购物车数据【包含合并过来的临时购物车的数据，和登录后的购物车的数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        } else {
            //没登录--- 的购物车
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
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
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
            return operations;
        } else {
            //临时购物车
            //--如果没登录--userInfoTo.getUserId() 为空 cartKey = dlq-mall:cart:userKey(uuid)
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
            operations.expire(7L, TimeUnit.DAYS);
            return operations;
        }
    }

    /**
     * 获取购物车里面的所有购物项
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map(obj -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 清空购物车数据
     */
    @Override
    public void clearCart(String cartkey) {
        redisTemplate.delete(cartkey);
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        if (check != null && cartItem != null) {
            BoundHashOperations<String, Object, Object> cartOps = getCartOps();
            cartItem.setCheck(check == 1);
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);
        }
    }

    @Override
    public void checkAllItem(List<Long> skuIds, Integer check) {
        if (skuIds != null && skuIds.size() > 0 && check !=null) {
            BoundHashOperations<String, Object, Object> cartOps = getCartOps();
            for (Long skuId : skuIds) {
                CartItem cartItem = getCartItem(skuId);
                if (cartItem != null){
                    cartItem.setCheck(check == 1);
                    String s = JSON.toJSONString(cartItem);
                    cartOps.put(skuId.toString(), s);
                }
            }
        }
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        if (skuId != null && num != null && cartItem != null) {
            if (num < 1) {
                num = 1;
            }
            cartItem.setCount(num);
            cartItem.setCheck(true);
            BoundHashOperations<String, Object, Object> cartOps = getCartOps();
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    /**
     * 删除购物项
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        if (skuId != null){
            BoundHashOperations<String, Object, Object> cartOps = getCartOps();
            cartOps.delete(skuId.toString());
        }
    }

    @Override
    public void deleteAllItem(List<Long> skuIds) {
        if (skuIds != null && skuIds.size()>0){
            for (Long skuId : skuIds) {
                BoundHashOperations<String, Object, Object> cartOps = getCartOps();
                cartOps.delete(skuId.toString());
            }
        }
    }

    @Override
    public List<CartItem> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==null){
            //没登录
            return null;
        }else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //查出购物车中所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            if (cartItems == null){
                //没东西刷新网页
                return null;
            }
            //进行过滤
            //获取所有被选中的购物项
            List<CartItem> collect = cartItems.stream()
                    .filter(CartItem::getCheck)
                    .map(item->{
                        BigDecimal price = productFeignService.getPrice(item.getSkuId());
                        //TODO 1、查询数据库更新为最新价格
                        item.setPrice(price);
                        return item;})
                    .collect(Collectors.toList());
            return collect;
        }
    }
}
