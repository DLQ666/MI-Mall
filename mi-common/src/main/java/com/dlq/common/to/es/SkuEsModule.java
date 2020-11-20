package com.dlq.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-19 19:01
 *
 */
@Data
public class SkuEsModule {

    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private boolean hasStock;

    private Long hotScore;

    private Long brandId;

    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    private List<Attrs> attrs;

    @Data
    public static class Attrs{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }


}
