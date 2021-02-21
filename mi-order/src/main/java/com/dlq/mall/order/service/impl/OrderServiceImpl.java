package com.dlq.mall.order.service.impl;

import com.dlq.common.vo.MemberRespVo;
import com.dlq.mall.order.feign.MemberFeignService;
import com.dlq.mall.order.interceptor.LoginUserInterceptor;
import com.dlq.mall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.order.dao.OrderDao;
import com.dlq.mall.order.entity.OrderEntity;
import com.dlq.mall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocalLoginUser.get();

        //1、远程查询所有的收货地址列表
        memberFeignService.getAddressEntities(memberRespVo.getId());
        //2、远程查询购物车所有选中的购物项

        return null;
    }

}
