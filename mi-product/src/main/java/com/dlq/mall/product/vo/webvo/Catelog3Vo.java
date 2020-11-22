package com.dlq.mall.product.vo.webvo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-22 13:38
 * 3级分类VO
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog3Vo {

    private String catalog2Id; //父分类 。二级分类id
    private String id;  //3级分类id
    private String name; //3级分类名称
}
