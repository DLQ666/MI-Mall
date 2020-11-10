package com.dlq.mall.search;

import com.alibaba.fastjson.JSON;
import com.dlq.mall.search.confg.ESConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;

@SpringBootTest
class MiSearchApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    //测试存储数据到es
    //更新也可以
    @Test
    public void testIndexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");//数据的id
//        indexRequest.source("userName","zhangsan","age",18,"gender","男");
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(18);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user); //将对象转成json
        indexRequest.source(jsonString, XContentType.JSON); //要保存的内容

        //执行操作
        IndexResponse index = restHighLevelClient.index(indexRequest, ESConfig.COMMON_OPTIONS);

        //提取有用的响应数据
        System.out.println(index);
    }

    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }
    
    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

}
