package com.dlq.mall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * RabbitMQ:
 * 1、自动导入amqp配置类：RabbitAutoConfiguration
 * 2、给容器中自动配置了
 *      RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 *      所有的属性都是
 *      @ConfigurationProperties(prefix = "spring.rabbitmq")
 *      public class RabbitProperties {...}
 * 3、给配置文件中配置 spring.rabbitmq 信息
 * 4、@EnableRabbit：@EnableXXX；开启功能
 * 5、监听消息：使用@RabbitListener；必须有@EnableRabbit
 *    @RabbitListener：标在 类或方法上（监听哪些队列）
 *    @RabbitHandler：标在 方法上（重载区分不同的信息）
 */
@EnableFeignClients(basePackages = "com.dlq.mall.order.feign")
@EnableRedisHttpSession
@EnableRabbit
@SpringBootApplication
public class MiOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiOrderApplication.class, args);
    }

}
