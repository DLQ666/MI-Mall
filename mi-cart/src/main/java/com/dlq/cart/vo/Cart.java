package com.dlq.cart.vo;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 *@program: MI-Mall
 *@description: 整个购物车  需要计算的属性，必须重写它的get方法，保证每次获取属性都会重新计算
 *@author: Hasee
 *@create: 2021-02-10 16:04
 */
public class Cart {

    List<CartItem> items;

    private Integer countNum; //商品数量

    private Integer countType; //商品类型数量

    private BigDecimal totalAmount; //商品总价

    private BigDecimal reduce = new BigDecimal("0");//减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size()>0){
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }


    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size()>0){
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 1、计算购物项总价
        if (items != null && items.size()>0){
            for (CartItem item : items) {
                if (item.getCheck() == true){
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        // 2、减去优惠总价
        BigDecimal subtract = amount.subtract(getReduce());
        return subtract;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
