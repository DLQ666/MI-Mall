package com.dlq.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.dlq.common.to.SkuReductionTo;
import com.dlq.common.to.mq.SeckillOrderTo;
import com.dlq.common.utils.R;
import com.dlq.common.vo.MemberRespVo;
import com.dlq.seckill.feign.CouponFeignService;
import com.dlq.seckill.feign.ProductFeignService;
import com.dlq.seckill.interceptor.LoginUserInterceptor;
import com.dlq.seckill.service.SeckillService;
import com.dlq.seckill.to.SeckillSkuRedisTo;
import com.dlq.seckill.vo.SeckillSessionsWithSkus;
import com.dlq.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-01 14:23
 */
@Slf4j
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
    @Autowired
    RabbitTemplate rabbitTemplate;

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

    public List<SeckillSkuRedisTo> blockHandler(BlockException e) {
        log.error("getCurrentSeckillSkus被限流了......");
        return null;
    }

    /**
     * blockHandLer函数会在原方法被限流/降级/系统保护的时候调用，而fallback函数会针对所有类型的异常。
     * @return
     */
    //返回当前时间可以参与秒杀商品信息
    @SentinelResource(value = "getCurrentSeckillSkus",blockHandler = "blockHandler")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前时间属于哪个秒杀场次；
        long time = new Date().getTime();
        try(Entry entry = SphU.entry("seckillSkus")){
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
        }catch(Exception e){
            log.error("资源被限流。。。。。getCurrentSeckillSkus，{}",e.getMessage());
        }

        return null;
    }

    /**
     * 获取某个sku商品的秒杀预告信息
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {

        //1、找到所有参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys!=null && keys.size()>0){
            //6_4  正则表达式匹配
            String regx = "\\d_"+skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)){
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    //随机码
                    long nowTime = new Date().getTime();
                    if (nowTime>=skuRedisTo.getStartTime() && nowTime<=skuRedisTo.getEndTime()){
                    }else {
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocalLoginUser.get();
        //1、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)){
            return null;
        }else {
            SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //校验合法性
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long now = new Date().getTime();

            long ttl = endTime-now;
            //1、校验时间的合法性
            if (!(now >= startTime && now <= endTime)) {
                return null;
            }
            //2、校验随机码和商品id
            String randomCode = redisTo.getRandomCode();
            String skuId = redisTo.getPromotionSessionId()+"_"+redisTo.getSkuId();
            if (!(randomCode.equals(key) && killId.equals(skuId))){
                return null;
            }
            //3、验证购物数量是否合理
            if (!(num<=redisTo.getSeckillLimit())){
                return null;
            }
            //4、验证这个人是否已近购买过。幂等性；如果秒杀成功，就去redis占位。  userId_SessionId_skuId
            //SETNX
            String redisKey = memberRespVo.getId()+"_"+skuId;
            //自动过期
            Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
            if (!aBoolean){
                //说明已经购买过了
                return null;
            }
            //占位成功说明从来没买过
            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
            //20ms
            boolean b = semaphore.tryAcquire(num);
            if (!b){
                return null;
            }
            //秒杀成功；
            //快速下单 发送MQ消息 10ms
            String timeId = IdWorker.getTimeId();
            SeckillOrderTo orderTo = new SeckillOrderTo();
            orderTo.setOrderSn(timeId);
            orderTo.setMemberId(memberRespVo.getId());
            orderTo.setNum(num);
            orderTo.setSeckillPrice(redisTo.getSeckillPrice());
            orderTo.setSkuId(redisTo.getSkuId());
            orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
            rabbitTemplate.convertAndSend("order-event-exchange", "order-seckill-order",orderTo);
            return timeId;
        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions){
        if (sessions != null)
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
