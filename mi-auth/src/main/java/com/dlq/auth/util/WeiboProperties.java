package com.dlq.auth.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-13 14:19
 */
@Data
@Component
//注意prefix要写到最后一个 "." 符号之前
@ConfigurationProperties(prefix="weibo.open")
public class WeiboProperties {
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String redirectUri;
}
