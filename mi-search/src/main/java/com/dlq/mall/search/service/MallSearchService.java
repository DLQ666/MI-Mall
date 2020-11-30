package com.dlq.mall.search.service;

import com.dlq.mall.search.vo.SearchParam;
import com.dlq.mall.search.vo.SearchResult;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-11-30 10:54
 */
public interface MallSearchService {

    /**
     * @param param 检索的所有参数
     * @return 检索的结果,包含页面需要的所有信息
     */
    SearchResult search(SearchParam param);
}
