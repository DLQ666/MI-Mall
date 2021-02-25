package com.dlq.mall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-25 12:03
 */
@Configuration
public class WareMQConfig {

    @Bean
    public Queue stockDelayQueue(){
        /**
         * x-dead-letter-exchange: stock-event-exchange
         * x-dead-letter-routing-key: stock.release
         * x-message-ttl: 60000*50
         */
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl", 60000*50);
        return new Queue("stock.delay.queue", true,false,false, arguments);
    }

    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue", true,false,false);
    }

    @Bean
    public Exchange stockEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Binding stockFinish(){
        //String destination, Binding.DestinationType destinationType,
        // String exchange, String routingKey, @Nullable Map<String, Object> arguments
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.finish",null);
    }
    @Bean
    public Binding stockRelease(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",null);
    }
}
