package com.dlq.seckill.controller;

import com.dlq.common.utils.R;
import com.dlq.seckill.service.SeckillService;
import com.dlq.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-01 17:17
 */
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return 秒杀商品信息
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId")Long skuId){
        SeckillSkuRedisTo skuRedisTo = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(skuRedisTo);
    }

    //?killId=6_129&key=8ad4f8977ebf40e78f55047166f22091&num=1
    @GetMapping("/kill")
    public String seckill(@RequestParam(value = "killId",required = false)String killId,
                          @RequestParam(value = "key",required = false)String key,
                          @RequestParam(value = "num",required = false)Integer num,
                          Model model){

        String orderSn = seckillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        //1、判断是否登录
        return "success";
    }
}
