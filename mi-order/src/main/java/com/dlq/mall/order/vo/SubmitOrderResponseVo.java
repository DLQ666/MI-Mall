package com.dlq.mall.order.vo;

import com.dlq.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-23 13:55
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;
    //状态码 0代表成功
    private Integer code;
}
