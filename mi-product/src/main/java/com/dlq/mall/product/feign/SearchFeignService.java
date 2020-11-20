package com.dlq.mall.product.feign;

import com.dlq.common.to.es.SkuEsModule;
import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-11-19 21:38
 */
@FeignClient("mi-search")
public interface SearchFeignService {

    //远程上架商品接口
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModule> skuEsModules);
}
