package com.dlq.mall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dlq.common.exception.BizCodeEnum;
import com.dlq.common.exception.NoStockException;
import com.dlq.mall.ware.vo.SkuHasStockVo;
import com.dlq.mall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dlq.mall.ware.entity.WareSkuEntity;
import com.dlq.mall.ware.service.WareSkuService;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.R;



/**
 * 商品库存
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 20:12:30
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try{
            Boolean stock = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch(NoStockException e){
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    //商品详情页，查询当前商品是否有库存
    @PostMapping("/{skuId}/hasstock")
    public R getSkuHasStock(@PathVariable("skuId") Long skuId){
        //sku_id  stock
        SkuHasStockVo vo = wareSkuService.getSkuIsStock(skuId);
        return R.ok().setData(vo);
    }

    //查询sku是否有库存
    @PostMapping("/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds){
        //sku_id  stock
        List<SkuHasStockVo> vos = wareSkuService.getSkuHasStock(skuIds);

        return R.ok().setData(vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
