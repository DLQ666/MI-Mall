package com.dlq.seckill;

import com.dlq.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class MiSeckillApplicationTests {

    @Autowired
    SeckillService seckillService;

    @Test
    void contextLoads() {
        //1、重复上架无需处理
        log.info("上架秒杀的商品信息...");
        //分布式锁 锁的业务执行完成，状态已经更新完成。释放锁以后。其他人获取到就会拿到最新的状态。
        seckillService.uploadSeckillSkuLatest3Days();
    }

}
