package com.dlq.mall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dlq.mall.ware.vo.MergeVo;
import com.dlq.mall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dlq.mall.ware.entity.PurchaseEntity;
import com.dlq.mall.ware.service.PurchaseService;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.R;



/**
 * 采购信息
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-11-04 15:00:15
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 完成采购单
     * /ware/purchase/done
     */
    @PostMapping("/done")
    public R finishPurchase(@RequestBody PurchaseDoneVo doneVo){
        purchaseService.donePurchase(doneVo);

        return R.ok();
    }

    /**
     * 领取采购单
     * /ware/purchase/received
     */
    @PostMapping("/received")
    public R receivedPurchase(@RequestBody List<Long> ids){
        purchaseService.receivedPurchase(ids);

        return R.ok();
    }


    /**
     * 查询未领取的采购单
     * @param params
     * @return
     * ///ware/purchase/unreceive/list
     */
    @RequestMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceive(params);

        return R.ok().put("page", page);
    }

    ///ware/purchase/merge
    /**
     * 合并采购需求
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo){
        purchaseService.mergePurchase(mergeVo);

        return R.ok();
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
        purchase.setUpdateTime(new Date());
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
