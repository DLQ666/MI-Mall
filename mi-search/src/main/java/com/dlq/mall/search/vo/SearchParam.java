package com.dlq.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-30 10:56
 * 封装页面所有了可能传递来的条件
 */
@Data
public class SearchParam {

    private String keyword; //搜索框 的全文检索匹配关键字

    private Long catalog3Id;//分类菜单点击3级分类id  传递来的关键字

    //sort = saleCount_asc/desc
    //sort = skuPrice_asc/desc
    //sort = hotScore_asc/desc
    private String sort; //排序条件

    /**过滤条件
     * hasStock(是否有货)、shuPrice区间、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     * brandId=1
     * attrs=2_5寸:6寸
     */
    private Integer hasStock = 1; //是否显示有货 0(表示无货) 1(表示有货)  默认是有货状态
    private String skuPrice;    //价格区间
    private List<Long> brandId; //按照品牌查询，可以多选
    private List<String> attrs; //按照属性进行筛选，可以多选
    private Integer pageNum = 1; //页码
}
