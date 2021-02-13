package com.dlq.auth.vo;

import lombok.Data;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-13 14:35
 */
@Data
public class SocialUser {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String udi;
    private String isRealName;
}
