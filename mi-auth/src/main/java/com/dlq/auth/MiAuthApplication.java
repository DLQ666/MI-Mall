package com.dlq.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * SpringSession 核心原理
 * 1）、@EnableRedisHttpSession  导入了 RedisHttpSessionConfiguration 配置
 *        1、RedisHttpSessionConfiguration 给容器中添加了一个组件
 *            SessionRepository==》【RedisIndexedSessionRepository】：redis操作session。session的增删改查封装类
 *        2、继承的 SpringHttpSessionConfiguration给容器中导入了 SessionRepositoryFilter ==》Filter： session存储的过滤器；每个请求过来都必须经过filter
 *            1、创建的时候，就自动从容器中获取到了SessionRepository；
 *            2、原生的request, response都被包装了 SessionRepositoryRequestWrapper，SessionRepositoryResponseWrapper
 *            3、以后获取session。肯定会调用-也就是原生方式获取 request.getSession()
 *            4、但事实是request被包装了放行的其实是wrappedRequest 也就是调用的SessionRepositoryRequestWrapper的重写的getSession()方法
 *                  wrappedRequest.getSession();==> SessionRepository 中获取到的。
 *  装饰者模式；
 */
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class MiAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiAuthApplication.class, args);
    }

}
