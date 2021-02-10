package com.dlq.mall.member.exception;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-08 12:32
 */
public class UsernameExistException extends RuntimeException{

    public UsernameExistException() {
        super("该用户名已存在");
    }
}
