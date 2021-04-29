package com.dlq.mall.product.feign.fallback;

import com.dlq.common.exception.BizCodeEnum;
import com.dlq.common.utils.R;
import com.dlq.mall.product.feign.OssFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-03 16:05
 */
@Slf4j
@Component
public class OssFileServiceFallback implements OssFileService {
    @Override
    public R removeLogo(String logo) {
        log.info("熔断方法调用.....removeLogo" );
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(),BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }
}
