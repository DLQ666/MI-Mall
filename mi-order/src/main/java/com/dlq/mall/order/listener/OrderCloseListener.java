package com.dlq.mall.order.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.dlq.mall.order.config.AlipayTemplate;
import com.dlq.mall.order.entity.OrderEntity;
import com.dlq.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-25 20:20
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;

    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息：准备关闭订单："+entity.getOrderSn());
        try{
            orderService.closeOrder(entity);
            //手动调用支付宝收单
            //获得初始化的AlipayClient
            AlipayClient alipayClient = new DefaultAlipayClient(alipayTemplate.getGatewayUrl(),
                    alipayTemplate.getApp_id(), alipayTemplate.getMerchant_private_key(),
                    "json", alipayTemplate.getCharset(), alipayTemplate.getAlipay_public_key(),
                    alipayTemplate.getSign_type());
            //设置请求参数
            AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
            //商户订单号，商户网站订单系统中唯一订单号
            String out_trade_no = entity.getOrderSn();
            //请二选一设置
            alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\"}");
            //请求
            String result = alipayClient.execute(alipayRequest).getBody();
            System.out.println("收单====》"+result);
            JSONObject jsonObject = JSON.parseObject(result);
            System.out.println(jsonObject);
            Map<String,String> response = (Map<String, String>) jsonObject.get("alipay_trade_close_response");
            String code = response.get("code");
            System.out.println(code);
            if ("10000".equals(code)){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }else if ("40004".equals(code)){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }else {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            }
        }catch(Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
