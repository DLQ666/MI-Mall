package com.dlq.mall.search.controller;

import com.dlq.common.to.es.SpuEsModule;
import com.dlq.common.utils.R;
import com.dlq.mall.search.service.MallSearchService;
import com.dlq.mall.search.vo.SearchParam;
import com.dlq.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Scanner;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-11-29 23:25
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 自动将页面提交过来的所有请求查询参数封装成指定的对象
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        param.set_queryString(request.getQueryString());
        //根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }

    @ResponseBody
    @GetMapping("/index/search/list")
    public R indexPage(SearchParam param){
        //根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(param);
        List<SpuEsModule> products = result.getProducts();
        return R.ok().put("result", products);
    }
}
