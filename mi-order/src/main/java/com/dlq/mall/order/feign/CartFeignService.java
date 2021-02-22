package com.dlq.mall.order.feign;

import com.dlq.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-02-21 23:01
 */
@FeignClient("mi-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
