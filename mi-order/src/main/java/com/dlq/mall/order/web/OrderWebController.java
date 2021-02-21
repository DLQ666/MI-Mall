package com.dlq.mall.order.web;

import com.dlq.mall.order.service.OrderService;
import com.dlq.mall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-21 20:04
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        //展示订单确认的数据
        return "detail";
    }
}
