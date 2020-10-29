package com.dlq.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dlq.common.constant.ProductConstant;
import com.dlq.mall.product.dao.AttrAttrgroupRelationDao;
import com.dlq.mall.product.dao.AttrGroupDao;
import com.dlq.mall.product.dao.CategoryDao;
import com.dlq.mall.product.entity.AttrAttrgroupRelationEntity;
import com.dlq.mall.product.entity.AttrGroupEntity;
import com.dlq.mall.product.entity.CategoryEntity;
import com.dlq.mall.product.service.CategoryService;
import com.dlq.mall.product.vo.AttrResponseVo;
import com.dlq.mall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.product.dao.AttrDao;
import com.dlq.mall.product.entity.AttrEntity;
import com.dlq.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        //1、保存基本数据
        this.save(attrEntity);
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId()!=null) {
            //2、保存关联关系
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            //attr_id  attr_name
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> responseVos = records.stream().map((attrEntity) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);
            //1、设置分类和分组的名字
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (relationEntity != null && relationEntity.getAttrGroupId()!=null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }
            return attrResponseVo;
        }).collect(Collectors.toList());

        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo responseVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, responseVo);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //1、设置分组信息
            AttrAttrgroupRelationEntity attrgroupRelationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrgroupRelationEntity != null) {
                responseVo.setAttrGroupId(attrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    responseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        //2、设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] categoryPath = categoryService.findCategoryPath(catelogId);
        responseVo.setCatelogPath(categoryPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            responseVo.setCatelogName(categoryEntity.getName());
        }
        return responseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //1、修改分组关联
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attr.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));

            if (count > 0) {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 根据分组id查找关联的所有基本属性（规格参数）
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntityList = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = relationEntityList.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }
        Collection<AttrEntity> entityList = this.listByIds(attrIds);
        return (List<AttrEntity>) entityList;
    }

    /**
     * 获取当前分组没有关联的所有属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2、当前分组只能关联其他分组没有引用的属性
        //2.1、当前分类下的其他分组
        List<AttrGroupEntity> attrGroup = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrGroupIdList = attrGroup.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        //2.2、这些分组关联的属性
        /*List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .in("attr_group_id", attrGroupIdList));*/
        QueryWrapper<AttrAttrgroupRelationEntity> attrAttrRelationEntityQueryWrapper = new QueryWrapper<>();
        if (attrGroupIdList != null && attrGroupIdList.size() > 0) {
            attrAttrRelationEntityQueryWrapper.in("attr_group_id", attrGroupIdList);
        }
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(attrAttrRelationEntityQueryWrapper);

        List<Long> attrIdsList = relationEntities.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //2.3、从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIdsList != null && attrIdsList.size() > 0) {
            wrapper.notIn("attr_id", attrIdsList);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

}
