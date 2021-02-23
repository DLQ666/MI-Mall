package com.dlq.common.exception;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-23 18:50
 */
public class NoStockException extends RuntimeException{

    private Long skuId;
    public NoStockException(String msg){
        super(msg);
    }
    public NoStockException(Long skuId){
        super("商品id："+skuId+"；没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
