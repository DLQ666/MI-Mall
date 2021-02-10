package com.dlq.mall.thirdparty.service;

import com.aliyuncs.exceptions.ClientException;

/**
 * @program: learn_parent
 * @description: 短信服务service层
 * @author: Hasee
 * @create: 2020-07-04 15:17
 */
public interface SmsService {

    /**
     * 阿里云短信接口
     * @param mobile 手机号
     * @param checkCode 验证码
     */
    void send(String mobile, String checkCode);
}
