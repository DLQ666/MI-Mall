package com.dlq.mall.order.web;

import com.dlq.common.exception.NoStockException;
import com.dlq.mall.order.service.OrderService;
import com.dlq.mall.order.vo.OrderConfirmVo;
import com.dlq.mall.order.vo.OrderSubmitVo;
import com.dlq.mall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

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
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        if (orderConfirmVo.getItems() == null || orderConfirmVo.getItems().size()==0){
            return "redirect:http://cart.dlqk8s.top:81/cart.html";
        }
        //展示订单确认的数据
        return "detail";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            System.out.println("订单提交的数据"+vo);
            if (responseVo.getCode() == 0){
                //下单成功，跳转到支付页
                model.addAttribute("submitOrderResp", responseVo);
                return "cashier";
            }else {
                String msg = "下单失败！";
                switch (responseVo.getCode()) {
                    case 1: msg += "订单信息过期，请刷新再次提交！"; break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交！"; break;
                    case 3: msg += "库存锁定失败，商品库存不足！"; break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                //下单失败，跳转到订单确认页面
                return "redirect:http://order.dlqk8s.top:81/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException){
                String message = ((NoStockException)e).getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
            //下单失败，跳转到订单确认页面
            return "redirect:http://order.dlqk8s.top:81/toTrade";
        }


    }
}
