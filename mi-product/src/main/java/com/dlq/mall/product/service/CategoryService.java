package com.dlq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlq.common.utils.PageUtils;
import com.dlq.mall.product.entity.CategoryEntity;
import com.dlq.mall.product.vo.webvo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 18:50:32
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMeunByIds(List<Long> asList);

    /**
     * 找到categoryId的完整路径【父/子/孙】
     * @param catelogId
     * @return
     */
    Long[] findCategoryPath(Long catelogId);

    void updateCascate(CategoryEntity category);

    //查询所有一级分类
    List<CategoryEntity> getLevel1Categorys();

    //查询所有分类
    Map<String, List<Catelog2Vo>> getCatalogJson();
}

