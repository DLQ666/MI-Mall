package com.dlq.cart.vo;

import lombok.Data;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-11 20:57
 */
@Data
public class UserInfoTo {

    private Long userId; //如果有用户ID说明用户登录了 ，如果没有那一定有临时用户 userKey
    private String userKey; //一定封装了

    private boolean tempUser = false;
}
