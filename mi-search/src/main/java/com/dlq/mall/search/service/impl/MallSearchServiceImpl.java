package com.dlq.mall.search.service.impl;

import com.dlq.mall.search.confg.ESConfig;
import com.dlq.mall.search.constant.EsConstant;
import com.dlq.mall.search.service.MallSearchService;
import com.dlq.mall.search.vo.SearchParam;
import com.dlq.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-30 10:54
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    //去es进行检索
    @Override
    public SearchResult search(SearchParam param) {
        //动态构建dsl语句
        SearchResult result = null;

        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, ESConfig.COMMON_OPTIONS);

            //分析响应数据，封装所需格式
            result = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //准备检索请求
    //#模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存)，排序，分页，高亮，聚合分析
    private SearchRequest buildSearchRequest(SearchParam param) {
        //创建构造DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存)
        //1、构建boolquery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1 构建must模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2 构建 bool filter  按照三级分类id查询
        if (param.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2 构建 bool filter 按照品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //1.2 构建 bool filter 按照指定属性进行查询
        //1.2 构建 bool filter 按照是否有库存进行查询
        boolQuery.filter(QueryBuilders.termsQuery("hasStock", param.getHasStock()==1));
        //1.2 构建 bool filter 按照价格区间进行查询
        if (!StringUtils.isEmpty(param.getSkuPrice())){
            //1_500/_500/500_
            //{
            //  "gte": 0,
            //  "lte": 1600
            //}
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2){
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if (s.length == 1){
                if (param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        sourceBuilder.query(boolQuery);
        //排序，分页，高亮

        //聚合分析

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},sourceBuilder);
        return searchRequest;
    }

    //构建结果数据
    private SearchResult buildSearchResult(SearchResponse response) {

        return null;
    }

}












