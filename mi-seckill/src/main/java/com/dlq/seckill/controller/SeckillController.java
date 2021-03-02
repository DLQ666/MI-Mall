package com.dlq.seckill.controller;

import com.dlq.common.utils.R;
import com.dlq.seckill.service.SeckillService;
import com.dlq.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-01 17:17
 */
@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }
}
