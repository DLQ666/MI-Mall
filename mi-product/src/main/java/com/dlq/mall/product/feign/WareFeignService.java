package com.dlq.mall.product.feign;

import com.dlq.common.to.SkuHasStockVo;
import com.dlq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-11-19 20:30
 */
@FeignClient("mi-ware")
public interface WareFeignService {

    /**
     * 1、R设计的时候可以加上泛型
     * 2、直接返回我们想要的结果
     * 3、自己封装解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    //商品详情页，查询当前商品是否有库存
    @PostMapping("/ware/waresku/{skuId}/hasstock")
    R getSkuHasStock(@PathVariable("skuId") Long skuId);
}
