package com.dlq.mall.thirdparty.service;

/**
 *@description:
 *@author: Hasee
 *@create: 2020-10-17 13:29
 */
public interface LogoService {

    /**
     * 阿里云oss文件删除
     * @param url 文件的URL地址
     */
    void removeLogo(String url);
}
