package com.dlq.mall.product.vo.webvo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-22 13:34
 * 2级分类VO
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {

    private String catalog1Id; //1级父分类id
    private List<Catelog3Vo> catalog3List; //三级子分类
    private String id;
    private String name;

}
