package com.dlq.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-21 11:48
 */
@Controller
public class HelloController {

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page")String page){
        return page;
    }
}
