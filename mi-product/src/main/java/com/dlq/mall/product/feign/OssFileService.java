package com.dlq.mall.product.feign;

import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-10-17 13:55
 */
@Service
@FeignClient(value = "mi-third-party")
public interface OssFileService {

    @RequestMapping("/oss/remove")
    R removeLogo(@RequestBody String logo);
}
