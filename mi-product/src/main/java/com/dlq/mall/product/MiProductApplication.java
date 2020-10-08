package com.dlq.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.dlq.mall.product.dao")
@SpringBootApplication
public class MiProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiProductApplication.class, args);
    }

}
