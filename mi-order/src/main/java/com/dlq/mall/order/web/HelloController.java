package com.dlq.mall.order.web;

import com.dlq.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-21 11:48
 */
@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrder(){
        //下单成功
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setModifyTime(new Date());
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        //发送消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderEntity);
        return "ok";
    }

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page")String page){
        return page;
    }
}
