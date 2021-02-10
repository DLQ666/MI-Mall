package com.dlq.mall.member.exception;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-08 12:32
 */
public class PhoneExistException extends RuntimeException{

    public PhoneExistException() {
        super("该手机号已注册");
    }
}
