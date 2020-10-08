package com.dlq.mall.member.feign;

import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-10-07 20:41
 */
@FeignClient("mi-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupons();
}
