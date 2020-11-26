package com.dlq.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-26 15:47
 */
@Configuration
public class MyRedissonConfig {

    @Value("${spring.redis.password}")
    private String password;

    //所有对Redisson的使用都是通过RedissonClient对象
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        //1、创建配置
        Config config = new Config();
        //Redis url should start with redis:// or rediss:// (for SSL connection)
        //可以用"rediss://"来启用SSL连接
        config.useSingleServer().setAddress("redis://gitdlq.top:6379").setPassword(password);

        //2、根据Config创建出RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
