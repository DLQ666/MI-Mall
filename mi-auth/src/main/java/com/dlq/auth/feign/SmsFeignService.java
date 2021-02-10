package com.dlq.auth.feign;

import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-07 14:38
 */
@FeignClient("mi-third-party")
public interface SmsFeignService {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);

}
