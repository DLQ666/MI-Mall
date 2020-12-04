package com.dlq.mall.search.feign;

import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-12-04 15:41
 */
@FeignClient("mi-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R brandInfo(@RequestParam("brandIds") List<Long> brandIds);

    @GetMapping("/product/brand/brandInfo")
    R brandInfoById(@RequestParam("brandId") Long brandId);
}
