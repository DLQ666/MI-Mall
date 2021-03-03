package com.dlq.seckill.service;

import com.dlq.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-03-01 14:23
 */
public interface SeckillService {

    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
