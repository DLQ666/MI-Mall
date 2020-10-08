package com.dlq.mall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MiCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiCouponApplication.class, args);
    }

}
