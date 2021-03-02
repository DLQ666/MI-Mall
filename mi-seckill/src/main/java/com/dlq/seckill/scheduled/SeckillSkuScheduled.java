package com.dlq.seckill.scheduled;

import com.dlq.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 *@program: MI-Mall
 *@description: 秒杀商品的定时上架
 *@author: Hasee
 *@create: 2021-03-01 14:13
 *
 * 每天晚上3点；上架最近3天需要秒杀的商品
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    //TODO 幂等性处理
    @Scheduled(cron = "0/3 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //1、重复上架无需处理
        log.info("上架秒杀的商品信息...");
        //分布式锁 锁的业务执行完成，状态已经更新完成。释放锁以后。其他人获取到就会拿到最新的状态。
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
