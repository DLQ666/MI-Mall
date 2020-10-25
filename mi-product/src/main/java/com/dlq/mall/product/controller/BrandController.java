package com.dlq.mall.product.controller;

import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.R;
import com.dlq.common.valid.AddGroup;
import com.dlq.common.valid.UpdateGroup;
import com.dlq.common.valid.UpdateStatusGroup;
import com.dlq.mall.product.entity.BrandEntity;
import com.dlq.mall.product.feign.OssFileService;
import com.dlq.mall.product.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 19:12:36
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;
    @Autowired
    OssFileService ossFileService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
        /*if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            //1、获取校验的错误结果
            result.getFieldErrors().forEach((item) -> {
                //FieldError 获取的错误提示
                String message = item.getDefaultMessage();
                //获取错误的属性名字
                String field = item.getField();
                map.put(field, message);

            });
            return R.error(400, "提交的数据不合法").put("data", map);
        } else {
        }*/
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改品牌状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));
        return R.ok();
    }

    /**
     * 修改或者添加品牌时选错图片  删除oss服务器中图片
     */
    @RequestMapping("/deleteLogo")
    public R deleteLogo(@RequestBody String url) {
        String substringUrl = url.substring(1, url.length() - 1);
        if (!StringUtils.isEmpty(substringUrl)) {
            ossFileService.removeLogo(substringUrl);
        }
        return R.ok();
    }
}
