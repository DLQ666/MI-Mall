package com.dlq.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dlq.common.to.es.SkuEsModule;
import com.dlq.common.to.es.SkuRes;
import com.dlq.common.to.es.SpuEsModule;
import com.dlq.common.utils.R;
import com.dlq.mall.search.confg.ESConfig;
import com.dlq.mall.search.constant.EsConstant;
import com.dlq.mall.search.feign.ProductFeignService;
import com.dlq.mall.search.service.MallSearchService;
import com.dlq.mall.search.vo.AttrResponseVo;
import com.dlq.mall.search.vo.BrandVo;
import com.dlq.mall.search.vo.SearchParam;
import com.dlq.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    ProductFeignService productFeignService;

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
            result = buildSearchResult(response,param);
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
        if (param.getAttrs()!=null&&param.getAttrs().size()>0){
            for (String attr : param.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_16G:8G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0]; //检索属性的id
                String[] attrValues = s[1].split(":"); //这个属性的检索用的值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //每一个必须生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        //1.2 构建 bool filter 按照是否有库存进行查询
        if (param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock()==1));
        }
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
                if ("".equals(s[0])){
                    s[0] = null;
                }
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if (s.length == 1){
                if (param.getSkuPrice().startsWith("_")){
                    rangeQuery.gte(null).lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //所有条件进行封装
        sourceBuilder.query(boolQuery);

        //排序，分页，高亮
        // 1、排序
        if (!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            //sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0],order);
        }
        //2、分页 pageSize：5
        //pageNum:1  from:0 size:5 【0,1,2,3,4】
        //pageNum：2 from：5 size；5 [5,6,7,8,9]
        //from = （pageNum-1）* size
        if (param.getPageNum() < 1) {
            param.setPageNum(1);
        }
        sourceBuilder.from((param.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //3、高亮
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:#e4393c'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        //聚合分析
        //1、品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //1.1品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //2、分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(50);
        //2.1 分类的子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        //3、属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合分析出当前attr_id对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //聚合分析出当前attr_id对应的属性值
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        //4、Spu商品聚合 及 聚合后的排序
        TermsAggregationBuilder spu_agg = AggregationBuilders.terms("spu_agg").field("spuId").size(500000);
        TermsAggregationBuilder spuImg = AggregationBuilders.terms("spuImg").field("skuImg");
        TopHitsAggregationBuilder top_img = AggregationBuilders.topHits("top_Img").fetchSource(new String[]{"skuImg", "skuTitle", "skuPrice", "skuId", "spuId"}, new String[]{}).size(1);
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=hotScore_asc/desc sort = saleCount_asc/desc sort = skuPrice_asc/desc
            String[] s = sort.split("_");
            if (s[0] != null && s[1] != null && s[1].equalsIgnoreCase("asc")) {
                MinAggregationBuilder minOrder = AggregationBuilders.min("min_Fields").field(s[0]);
                spu_agg = AggregationBuilders.terms("spu_agg").field("spuId").size(500000).order(BucketOrder.aggregation("min_Fields",true));
                spu_agg.subAggregation(minOrder);
                top_img = AggregationBuilders.topHits("top_Img").fetchSource(new String[]{"skuImg", "skuTitle", "skuPrice", "skuId", "spuId"}, new String[]{}).size(1).sort(s[0], SortOrder.ASC);
            }else if (s[0] != null && s[1] != null && s[1].equalsIgnoreCase("desc")){
                MaxAggregationBuilder maxOrder = AggregationBuilders.max("min_Fields").field(s[0]);
                spu_agg = AggregationBuilders.terms("spu_agg").field("spuId").size(500000).order(BucketOrder.aggregation("min_Fields",false));
                spu_agg.subAggregation(maxOrder);
                top_img = AggregationBuilders.topHits("top_Img").fetchSource(new String[]{"skuImg", "skuTitle", "skuPrice", "skuId", "spuId"}, new String[]{}).size(1).sort(s[0], SortOrder.DESC);
            }
        }
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:#e4393c'>");
            highlightBuilder.postTags("</b>");
            top_img.highlighter(highlightBuilder);
        }
        spuImg.subAggregation(top_img);
        spu_agg.subAggregation(spuImg);
        sourceBuilder.aggregation(spu_agg);

        System.out.println("构建的DSL语句："+sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},sourceBuilder);
        return searchRequest;
    }

    //构建结果数据
    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {
        SearchResult result = new SearchResult();
        //1、返回所有查询到的聚合Spu商品
        ParsedStringTerms spu_agg = response.getAggregations().get("spu_agg");
        List<? extends Terms.Bucket> spuAggBuckets = spu_agg.getBuckets();
        List<SpuEsModule> spuEsModules = new ArrayList<>();
        for (Terms.Bucket spuAggBucket : spuAggBuckets) {
            SpuEsModule spuEsModule = new SpuEsModule();
            long spuId = spuAggBucket.getKeyAsNumber().longValue();
            spuEsModule.setSpuId(spuId);
            List<SpuEsModule.SpuAttr> SpuAttrs = new ArrayList<>();
            List<? extends Terms.Bucket> spuImg = ((ParsedStringTerms) spuAggBucket.getAggregations().get("spuImg")).getBuckets();
            for (Terms.Bucket item : spuImg) {
                ParsedTopHits topHits = item.getAggregations().get("top_Img");
                SearchHit at = topHits.getHits().getAt(0);
                String sourceAsString = at.getSourceAsString();
                SkuRes SkuRes = JSON.parseObject(sourceAsString, SkuRes.class);
                //设置高亮
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField highlightField = at.getHighlightFields().get("skuTitle");
                    String string = highlightField.getFragments()[0].string();
                    SkuRes.setSkuTitle(string);
                }
                SpuEsModule.SpuAttr spuAttr = new SpuEsModule.SpuAttr();
                spuAttr.setSkuId(SkuRes.getSkuId());
                spuAttr.setSkuImg(SkuRes.getSkuImg());
                spuAttr.setSkuPrice(SkuRes.getSkuPrice());
                spuAttr.setSkuTitle(SkuRes.getSkuTitle());
                SpuAttrs.add(spuAttr);
                spuEsModule.setAttrs(SpuAttrs);
                if (spuEsModule.getDefImg() == null && SkuRes.getSkuImg() != null) {
                    spuEsModule.setDefImg(SkuRes.getSkuImg());
                }
                if (spuEsModule.getDefSkuId() == null && SkuRes.getSkuId() != null){
                    spuEsModule.setDefSkuId(SkuRes.getSkuId());
                }
                if (spuEsModule.getDefTitle() == null && SkuRes.getSkuTitle() != null){
                    spuEsModule.setDefTitle(SkuRes.getSkuTitle());
                }
            }
            spuEsModules.add(spuEsModule);
        }
        result.setProducts(spuEsModules);

        //2、当前所有商品涉及到的所有属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attr_id_agg.getBuckets();
        for (Terms.Bucket attrBucket : attrIdAggBuckets) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //得到属性id
            /*String attrId = attrBucket.getKeyAsString();
            attrVo.setAttrId(Long.parseLong(attrId));*/
            //或者attrBucket.getKeyAsString() 直接转为数字
            long attrId = attrBucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //得到属性名字
            /*ParsedStringTerms attr_name_agg = attrBucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();*/
            //也可以这么写
            String attrName = ((ParsedStringTerms) attrBucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            //得到属性值
            /*ParsedStringTerms attr_value_agg = attrBucket.getAggregations().get("attr_value_agg");
            List<? extends Terms.Bucket> attrValueAggBuckets = attr_value_agg.getBuckets();
            List<String> attrValue = new ArrayList<>();
            for (Terms.Bucket attrValueAggBucket : attrValueAggBuckets) {
                String attr = attrValueAggBucket.getKeyAsString();
                attrValue.add(attr);
            }*/
            //用stream API编写
            List<String> attrValue = ((ParsedStringTerms) attrBucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValue);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        //3、当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> brandAggBuckets = brand_agg.getBuckets();
        for (Terms.Bucket brandAggBucket : brandAggBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //得到品牌id
            String keyAsString = brandAggBucket.getKeyAsString();
            brandVo.setBrandId(Long.parseLong(keyAsString));

            //得到品牌名
            ParsedStringTerms brand_name_agg = brandAggBucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            //得到品牌图片
            ParsedStringTerms brand_img_agg = brandAggBucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4、当前所有商品涉及到的所有分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);

            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);


        //6、分页-总记录数
        long total = spuAggBuckets.size();
        result.setTotal(total);

        //7、分页-总页码-计算得到
        int totalPages = (int)total%EsConstant.PRODUCT_PAGESIZE==0?(int)total/EsConstant.PRODUCT_PAGESIZE:((int)total/EsConstant.PRODUCT_PAGESIZE+1);
        result.setTotalPages(totalPages);

        //5、分页-当前页码
        result.setPageNum(param.getPageNum());

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        /*//构建属性面包屑导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //分析每一个attrs传来的查询参数值  attrs=2_5寸:6寸
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                //根据id查询属性分类名字---远程调用商品服务---按照属性id查询属性详细信息
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0){
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(data.getAttrName());
                }else {
                    navVo.setNavName(s[0]);
                }
                //取消面包屑，我们要跳转到那个地方，将请求地址的url里面的当前条件置空
                //拿到所有的查询条件，去掉当前条件  attrs  =  2_iPhone 12
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.dlqk8s.top:81/list.html?"+replace);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(collect);
        }*/

        List<SearchResult.NavVo> collect = new ArrayList<>();

        //构建品牌面包屑导航
        if (param.getBrandId()!=null && param.getBrandId().size()>0){
            StringBuffer buffer = new StringBuffer();
            collect = param.getBrandId().stream().map(brandById -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                navVo.setNavName("品牌");
                R r = productFeignService.brandInfoById(brandById);
                String replace = "";
                if (r.getCode() == 0) {
                    BrandVo brand = r.getData("brand", new TypeReference<BrandVo>() {
                    });
                    if (brand != null){
                        buffer.append(brand.getName());
                        replace = replaceQueryString(param, brand.getBrandId() + "", "brandId");
                    }
                } else {
                    navVo.setNavValue(String.valueOf(brandById));
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.dlqk8s.top:81/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());

            /*List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("品牌");
            R r = productFeignService.brandInfo(param.getBrandId());
            if (r.getCode() == 0){
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getName());
                    replace = replaceQueryString(param,brandVo.getBrandId()+"","brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.dlqk8s.top:81/list.html?"+replace);
            }
            navs.add(navVo);*/
        }

        //构建属性面包屑导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> finalCollect = collect;
            param.getAttrs().stream().map(attr -> {
                //分析每一个attrs传来的查询参数值  attrs=2_5寸:6寸
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                //根据id查询属性分类名字---远程调用商品服务---按照属性id查询属性详细信息
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                //取消面包屑，我们要跳转到那个地方，将请求地址的url里面的当前条件置空
                //拿到所有的查询条件，去掉当前条件  attrs  =  2_iPhone 12
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.dlqk8s.top:81/list.html?" + replace);
                finalCollect.add(navVo);
                return navVo;
            }).collect(Collectors.toList());
        }
        result.setNavs(collect);

        return result;
    }

    private String replaceQueryString(SearchParam param, String value,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            //浏览器对空格编码和java不一样，所以java将空格替换为+号，就把+号替换成浏览器的%20
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&"+key+"="+encode,"");
    }

}












