package com.dlq.mall.product.feign;

import com.dlq.common.to.SkuReductionTo;
import com.dlq.common.to.SpuBoundsTo;
import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-10-31 15:11
 */
@FeignClient("mi-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);


    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
