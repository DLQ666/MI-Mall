package com.dlq.mall.order.feign;

import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-02-22 20:03
 */
@FeignClient("mi-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
