package com.dlq.mall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.dlq.mall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-10-29 19:53
 */
@Data
public class AttrGrooupWithAttrsVo {

    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 属性信息
     */
    private List<AttrEntity> attrs;
}
