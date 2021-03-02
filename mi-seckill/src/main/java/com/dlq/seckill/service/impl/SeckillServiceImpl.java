package com.dlq.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dlq.common.utils.R;
import com.dlq.seckill.feign.CouponFeignService;
import com.dlq.seckill.feign.ProductFeignService;
import com.dlq.seckill.service.SeckillService;
import com.dlq.seckill.to.SeckillSkuRedisTo;
import com.dlq.seckill.vo.SeckillSessionsWithSkus;
import com.dlq.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-01 14:23
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // +商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1、扫描最近三天需要参与的秒杀的活动。
        R session = couponFeignService.getLatest3DaySession();
        if (session.getCode() == 0){
            //上架商品
            List<SeckillSessionsWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到Redis
            //1、缓存活动信息
            saveSessionInfos(sessionData);
            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    //返回当前时间可以参与秒杀商品信息
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前时间属于哪个秒杀场次；
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            //seckill:sessions:1614578400000_1614582000000
            String replace = key.replace(SESSION_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time>=start && time<=end){
                //2、获取这个秒杀场次需要的所有商品
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, Object> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<Object> list = hashOps.multiGet(range);
                if (list!=null){
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
                        //redisTo.setRandomCode(null); 当前秒杀开始了就需要随机码
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions){
        sessions.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;

            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (!hasKey) {
                List<String> collect = session.getRelationEntities().stream().map(item -> item.getPromotionSessionId()+"_"+item.getSkuId().toString()).collect(Collectors.toList());
                //缓存活动信息
                stringRedisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions){
        sessions.stream().forEach(session->{
            //准备hash操作
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationEntities().stream().forEach(seckillSku->{

                //4、 随机码？
                String token = UUID.randomUUID().toString().replace("-", "");

                if (!hashOps.hasKey(seckillSku.getPromotionSessionId().toString()+"_"+seckillSku.getSkuId().toString())){
                    //缓存商品
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    //1、sku基本信息
                    R info = productFeignService.getSkuInfo(seckillSku.getSkuId());
                    if (info.getCode() == 0){
                        SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuRedisTo.setSkuInfoVo(skuInfo);
                    }

                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(seckillSku, seckillSkuRedisTo);

                    //3、设置上 当前商品的秒杀时间信息
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    seckillSkuRedisTo.setRandomCode(token);

                    String jsonString = JSON.toJSONString(seckillSkuRedisTo);
                    hashOps.put(seckillSku.getPromotionSessionId().toString()+"_"+seckillSku.getSkuId().toString(),jsonString);

                    //如果当前这个场次的商品的库存信息已经上架就不需要上架
                    //5、使用库存作为分布式的信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSku.getSeckillCount());
                }
            });
        });
    }
}
