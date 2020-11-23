package com.dlq.mall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.mall.product.entity.BrandEntity;
import com.dlq.mall.product.service.BrandService;
import com.dlq.mall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
class MiProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis() {
        //保存一个Hello  world
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //保存
        ops.set("hello", "world"+ UUID.randomUUID().toString());
        //查询
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void test01() {
        Long[] categoryPath = categoryService.findCategoryPath(452L);
        log.info("完整路径：{}", Arrays.asList(categoryPath));
    }

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
