package com.dlq.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.dlq.mall.product.entity.AttrEntity;
import com.dlq.mall.product.service.AttrAttrgroupRelationService;
import com.dlq.mall.product.service.AttrService;
import com.dlq.mall.product.service.CategoryService;
import com.dlq.mall.product.vo.AttrGrooupRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dlq.mall.product.entity.AttrGroupEntity;
import com.dlq.mall.product.service.AttrGroupService;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.R;



/**
 * 属性分组
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 19:12:36
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    ///product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entityList =  attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",entityList);
    }

    ///product/attrgroup/attr/relation
    @PostMapping("/attr/relation")
    public R addAttrRelation(@RequestBody List<AttrGrooupRelationVo> relationVo){
        relationService.addBatchRelation(relationVo);
        return R.ok();
    }

    ///product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils page =  attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCategoryPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    ///product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGrooupRelationVo[] vos){
        attrGroupService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
