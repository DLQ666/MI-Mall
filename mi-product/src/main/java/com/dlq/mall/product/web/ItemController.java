package com.dlq.mall.product.web;

import com.dlq.mall.product.service.SkuInfoService;
import com.dlq.mall.product.vo.sku.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-01-30 15:44
 */
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId , Model model){
        System.out.println("准备查询"+skuId +"的详情");
        SkuItemVo skuItemVo = skuInfoService.itemInfo(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
