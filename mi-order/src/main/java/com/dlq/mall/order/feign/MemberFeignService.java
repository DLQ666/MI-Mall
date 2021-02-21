package com.dlq.mall.order.feign;

import com.dlq.mall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2021-02-21 22:08
 */
@FeignClient("mi-member")
public interface MemberFeignService {

    @GetMapping("/{memberId}/address")
    List<MemberAddressVo> getAddressEntities(@PathVariable("memberId") Long memberId);
}
