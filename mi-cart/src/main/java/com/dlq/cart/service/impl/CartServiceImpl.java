package com.dlq.cart.service.impl;

import com.dlq.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-11 12:26
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;



}
