package com.dlq.mall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.mall.product.entity.BrandEntity;
import com.dlq.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MiProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        /*brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功。。。");*/

        /*brandEntity.setBrandId(1L);
        brandEntity.setDescript("hahahahhaha");
        brandService.updateById(brandEntity);*/

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach(brandEntity1 -> {
            System.out.println(brandEntity1);
        });
    }

}
