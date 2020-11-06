package com.dlq.mall.ware.feign;

import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-06 14:42
 */
@FeignClient("mi-product")
public interface ProductFeignService {

    /**
     *  1、请求经过网关
     *      @FeignClient("mi-gateway)
     *      /api/product/skuinfo/info/{skuId}
     *  2、请求不经过网关
     *      @FeignClient("mi-product")
     *      /product/skuinfo/info/{skuId}
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
