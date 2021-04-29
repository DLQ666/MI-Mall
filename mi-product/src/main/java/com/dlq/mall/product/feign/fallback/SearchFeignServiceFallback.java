package com.dlq.mall.product.feign.fallback;

import com.dlq.common.exception.BizCodeEnum;
import com.dlq.common.to.es.SkuEsModule;
import com.dlq.common.utils.R;
import com.dlq.mall.product.feign.SearchFeignService;
import com.dlq.mall.product.vo.SearchParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-03 16:01
 */
@Slf4j
@Component
public class SearchFeignServiceFallback implements SearchFeignService {

    @Override
    public R productStatusUp(List<SkuEsModule> skuEsModules) {
        log.info("熔断方法调用.....productStatusUp" );
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(),BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }

    @Override
    public R indexPage(SearchParam param) {
        log.info("熔断方法调用.....indexPage" );
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(),BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }
}
