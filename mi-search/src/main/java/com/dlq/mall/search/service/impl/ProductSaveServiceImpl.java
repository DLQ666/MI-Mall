package com.dlq.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.dlq.common.to.es.SkuEsModule;
import com.dlq.mall.search.confg.ESConfig;
import com.dlq.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-19 21:13
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModule> skuEsModules) throws IOException {
        //保存到es
        //1、给Es中建立索引。product，建立好映射关系

        //2、给es中保存这些数据
        // BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModule skuEsModule : skuEsModules) {
            //构造保存请求
            IndexRequest indexRequest = new IndexRequest("product");
            indexRequest.id(skuEsModule.getSkuId().toString());

            String s = JSON.toJSONString(skuEsModule);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ESConfig.COMMON_OPTIONS);

        //TODO 如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架完成：{}", collect);

        return b;
    }
}
