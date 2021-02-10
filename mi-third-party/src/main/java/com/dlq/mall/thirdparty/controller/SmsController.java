package com.dlq.mall.thirdparty.controller;

import com.aliyuncs.exceptions.ClientException;
import com.dlq.common.utils.R;
import com.dlq.mall.thirdparty.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-07 13:50
 */
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    /**
     * 提供给别的服务进行调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        smsService.send(phone,code);
        return R.ok();
    }
}
