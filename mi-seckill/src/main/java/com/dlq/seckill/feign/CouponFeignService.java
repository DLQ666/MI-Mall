package com.dlq.seckill.feign;

import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-01 14:25
 */
@FeignClient("mi-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();

}
