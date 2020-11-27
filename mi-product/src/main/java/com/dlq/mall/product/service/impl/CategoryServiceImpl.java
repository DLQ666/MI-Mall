package com.dlq.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dlq.mall.product.entity.CategoryBrandRelationEntity;
import com.dlq.mall.product.service.CategoryBrandRelationService;
import com.dlq.mall.product.vo.webvo.Catelog2Vo;
import com.dlq.mall.product.vo.webvo.Catelog3Vo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.product.dao.CategoryDao;
import com.dlq.mall.product.entity.CategoryEntity;
import com.dlq.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> categoryList = baseMapper.selectList(null);
        //2、组装成父子的树形结构
        //2.1）、找到所有一级分类
        List<CategoryEntity> level1Menus = categoryList.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, categoryList));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMeunByIds(List<Long> asList) {
        //TODO: 检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);
        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    //级联更新所有关联的数据
    @Transactional
    @Override
    public void updateCascate(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }
    }

    //查询所有一级分类
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0).orderByAsc("sort"));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        //给缓存中放入JSON字符串，拿出的json字符串，还用逆转未来能用的对象类型，【序列化与反序列化】
        //好处：JSON是跨语言，跨平台兼容的。
        //1、加入缓存逻辑，存入的是json字符串
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)){
            //2、判断缓存中没有数据，就从数据库查询
            System.out.println("缓存不命中，查询数据库。。。");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            return catalogJsonFromDb;
        }
        System.out.println("缓存命中。。。");
        //转为我们指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson,new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    //从数据库查询并封装分类数据-----加Redis分布式锁
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //只要是同一把锁，就能锁住需要这个的所有线程
        //在分布式情况下，想要锁住所有，必须使用分布式锁
        //1、占分布式锁，去Redis占坑
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();
        //加锁成功，执行业务-查询数据库
        System.out.println("获取分布式锁成功。。。。");
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        }finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    //从数据库查询并封装分类数据-----加Redis分布式锁
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //只要是同一把锁，就能锁住需要这个的所有线程
        //在分布式情况下，想要锁住所有，必须使用分布式锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock){
            //加锁成功，执行业务-查询数据库
            System.out.println("获取分布式锁成功。。。。");
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            }finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" + "then\n" + "    return redis.call(\"del\",KEYS[1])\n" + "else\n" + "    return 0\n" + "end";
                //原子删锁-----lua脚本解锁
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class), Arrays.asList("lock"), uuid);
            }

            /*//获取值对比+对比成功后删除====原子操作，，，，下面的不能保证原子操作-----》如何解决，官网说使用 Lua脚本解锁
            String lockValue = stringRedisTemplate.opsForValue().get("lock");
            if (uuid.equals(lockValue)){
                //删除锁
                stringRedisTemplate.delete("lock");
            }*/
            return dataFromDb;
        }else {
            //加锁失败.....可以等待..以自旋的方式
            System.out.println("获取分布式锁失败.....等待重试");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //得到锁以后，我们应该再去缓存中查询确定一次，如果没有才需要继续查询。
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)){
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson,new TypeReference<Map<String, List<Catelog2Vo>>>(){});
            return result;
        }
        System.out.println("查询了数据库。。。。");
        /*
          优化一：将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().orderByAsc("sort"));

        //查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList,0L);

        //封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 遍历每个一级分类，查询每个一级分类的二级分类 v为每个一级分类对象
            List<CategoryEntity> categoryEntities = getParent_cid(selectList,v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类的三级分类封装成VO
                    List<CategoryEntity> category3List = getParent_cid(selectList,l2.getCatId());
                    if (category3List != null){
                        List<Catelog3Vo> catelog3Vos = category3List.stream().map(l3 -> {
                            //不为空，封装成指定格式vo
                            Catelog3Vo catelog3Vo = new Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        //3、保存在Redis中，将对象转为JSon放在缓存中
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJson", s);
        return parent_cid;
    }

    //从数据库查询并封装分类数据-----加本地锁
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        //只要是同一把锁，就能锁住需要这个的所有线程
        //synchronized (this)；springboot所有的组件再容纳器中都是单例的
        //本地锁: synchronized， JUC (Lock)，在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this){
            System.out.println("查询了数据库。。。。");
            return getDataFromDb();
        }
    }

    public List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        return collect;
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }

    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId == null){
            return paths;
        }
        if (byId.getParentCid() !=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}
