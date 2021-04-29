package com.dlq.mall.product.feign;

import com.dlq.common.to.es.SkuEsModule;
import com.dlq.common.utils.R;
import com.dlq.mall.product.feign.fallback.SearchFeignServiceFallback;
import com.dlq.mall.product.vo.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-11-19 21:38
 */
@FeignClient(value = "mi-search",fallback = SearchFeignServiceFallback.class)
public interface SearchFeignService {

    //远程上架商品接口
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModule> skuEsModules);

    //远程查询商品接口
    @GetMapping("/index/search/list")
    R indexPage(@RequestParam("param") SearchParam param);
}
