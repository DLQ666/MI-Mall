package com.dlq.mall.search.service;

import com.dlq.common.to.es.SkuEsModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-11-19 21:12
 */
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModule> skuEsModules) throws IOException;
}
