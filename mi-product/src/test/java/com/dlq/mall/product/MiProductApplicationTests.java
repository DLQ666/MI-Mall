package com.dlq.mall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlq.mall.product.dao.AttrGroupDao;
import com.dlq.mall.product.dao.SkuSaleAttrValueDao;
import com.dlq.mall.product.entity.BrandEntity;
import com.dlq.mall.product.entity.CategoryEntity;
import com.dlq.mall.product.service.BrandService;
import com.dlq.mall.product.service.CategoryService;
import com.dlq.mall.product.service.impl.CategoryServiceImpl;
import com.dlq.mall.product.vo.sku.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
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

    @Autowired
    CategoryServiceImpl service;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test() {
        System.out.println(skuSaleAttrValueDao.getSaleAttrsBySpuId(9L));
    }

    @Test
    public void test1() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(9L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

    @Test
    public void testRedissonTest() {
        System.out.println(redissonClient);
    }

    public static void main(String[] args) {
        String aaa ="羽绒服棉服运动裤夹克/风衣卫衣/套头衫T恤套装乒羽网服健身服运动背心毛衫/线衫运动配饰";
        String bbb ="智能手环智能手表智能眼镜运动跟踪器健康监测智能配饰智能家居体感车其他配件无人机智能机器人";
        System.out.println(aaa.length());
        System.out.println(bbb.length());
    }

    @Test
    public void test000() {
        List<CategoryEntity> sort = categoryService.list();
        System.out.println(sort);
        List<CategoryEntity> parent_cid = service.getParent_cid(sort, 1434L);
        System.out.println(parent_cid);
    }

    @Test
    public void test0001() {
        String aaa ="移动电源电池/移动电源蓝牙耳机充电器/数据线苹果周边手机耳机手机贴膜手机存储卡充电器数据线手机保护套车载配件iPhone 配件手机电池创意配件便携/无线音响手机饰品拍照配件手机支架";
        System.out.println(aaa.length());
    }

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
