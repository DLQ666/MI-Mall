package com.dlq.mall.order;

import com.dlq.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest
class MiOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /*@Test
    public void test() {
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setSort(1);
        reasonEntity.setName("哈哈哈张三");
        //1、发送消息，如果发送的消息是个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable接口
        String msg = "Hello World!";

        //2、发送的对象类型的消息,可以是一个JSON
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",reasonEntity);
        log.info("消息发送完成：{}", reasonEntity);
    }*/

    /**
     * 1、如何创建Exchange[hello-java-exchange]、Queue、Binding
     *      1）、使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    /*@Test
    public void createExchange() {
        *//**
         * public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         *//*
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建完成", "hello-java-exchange");
    }*/

    /*@Test
    public void createQueue() {
        *//**
         * public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
         * name：队列名字    durable：是否持久化   exclusive：是否排他，只有连接队列的才能手收到消息
         * autoDelete：是否自动删除    arguments：指定参数
         *//*
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建完成", "hello-java-queue");
    }*/

    /*@Test
    public void createBinding() {
        *//**
         * public Binding(
         * String destination,【目的地】：
         * Binding.DestinationType destinationType,【目的地类型】：
         * String exchange,【交换机】：
         * String routingKey,【路由键】：
         * @Nullable Map<String, Object> arguments【自定义参数】：)
         *
         * 将exchange【交换机】指定的交换机和destination【目的地】进行绑定，使用routingKey作为指定的路由键
         *//*
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建完成", "hello-java-binding");
    }*/

}
