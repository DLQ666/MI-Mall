package com.dlq.mall.order.controller;

import com.dlq.mall.order.entity.OrderEntity;
import com.dlq.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-20 15:01
 */
@Slf4j
@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMQ")
    public String sendMQ(@RequestParam(value = "num",defaultValue = "10")Integer num){
        for (int i = 0; i < 10; i++) {
            if (i%2 == 0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("哈哈->"+i);
                /**
                 * public void convertAndSend(String exchange, String routingKey, final Object object,
                 *                        @Nullable CorrelationData correlationData)
                 */
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",reasonEntity,
                        new CorrelationData(UUID.randomUUID().toString()));
            }else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange","hello22.java",orderEntity,
                        new CorrelationData(UUID.randomUUID().toString()));
            }
        }
        return "ok";
    }

}
