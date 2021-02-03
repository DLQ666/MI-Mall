package com.dlq.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-02 11:46
 */
@Data
public class SpuEsModule {

    private Long spuId;
    private String defImg;
    private Long defSkuId;
    private String defTitle;
    private List<SpuAttr> attrs;

    @Data
    public static class SpuAttr{
        private Long skuId;

        private String skuTitle;

        private BigDecimal skuPrice;

        private String skuImg;
    }

}
