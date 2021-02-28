package com.dlq.mall.member.web;

import com.alibaba.fastjson.JSON;
import com.dlq.common.utils.R;
import com.dlq.mall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-27 20:55
 */
@Controller
public class MemberWebController {
    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/list.html")
    public String memberOrderList(@RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                  Model model, HttpServletRequest request){
        //查出当前登录用户的所有订单列表数据
        Map<String,Object> map = new HashMap<>();
        map.put("page", pageNum);
        R r = orderFeignService.listWithItem(map);
        model.addAttribute("orders", r);
        System.out.println(JSON.toJSONString(r));
        return "list";
    }
}
