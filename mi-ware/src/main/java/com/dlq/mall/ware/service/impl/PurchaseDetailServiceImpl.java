package com.dlq.mall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.ware.dao.PurchaseDetailDao;
import com.dlq.mall.ware.entity.PurchaseDetailEntity;
import com.dlq.mall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();
        //status: 1
        //wareId: 1
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            //purchase_id  sku_id
            queryWrapper.and((wrapper)->{
                wrapper.eq("purchase_id", key).or().eq("sku_id", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            //purchase_id  sku_id
            queryWrapper.eq("status", status);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            //purchase_id  sku_id
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {

        List<PurchaseDetailEntity> purchaseId = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        return purchaseId;
    }

}
