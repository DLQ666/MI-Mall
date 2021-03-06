package com.dlq.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.dlq.mall.product.feign")
@MapperScan("com.dlq.mall.product.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class MiProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiProductApplication.class, args);
    }

}
