package com.dlq.mall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.dlq.common.enums.OrderStatusEnum;
import com.dlq.common.to.mq.StockDetailTo;
import com.dlq.common.to.mq.StockLockedTo;
import com.dlq.common.utils.R;
import com.dlq.mall.ware.entity.WareOrderTaskDetailEntity;
import com.dlq.mall.ware.entity.WareOrderTaskEntity;
import com.dlq.mall.ware.feign.OrderFeignService;
import com.dlq.mall.ware.service.WareSkuService;
import com.dlq.mall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-25 18:19
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息...");
        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
