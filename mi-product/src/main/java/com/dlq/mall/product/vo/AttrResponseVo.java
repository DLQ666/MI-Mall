package com.dlq.mall.product.vo;

import lombok.Data;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-10-26 21:23
 */
@Data
public class AttrResponseVo extends AttrVo{

//          "catelogName": "手机/数码/手机", //所属分类名字
//			"groupName": "主体", //所属分组名字

    private String catelogName;
    private String groupName;
}
