package com.dlq.mall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-05 16:58
 */
@Data
@Component
@ConfigurationProperties(prefix = "mall.thread")
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
