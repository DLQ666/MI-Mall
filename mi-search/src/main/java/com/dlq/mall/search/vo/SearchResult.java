package com.dlq.mall.search.vo;

import com.dlq.common.to.es.SkuEsModule;
import lombok.Data;

import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-30 11:24
 */
@Data
public class SearchResult {

    //查询到的所有商品信息
    private List<SkuEsModule> products;

    private List<BrandVo> brands;//当前查询到的结果，所有涉及到的品牌
    private List<CatalogVo> catalogs; //当前查询到的结果。所有涉及到的分类
    private List<AttrVo> attrs; // 当前查询到的结果，所有涉及到的所有属性

    //分页信息
    private Integer pageNum;//当前页码
    private Long total; //总记录数
    private Integer totalPages; //总页码



    //品牌信息vo
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}