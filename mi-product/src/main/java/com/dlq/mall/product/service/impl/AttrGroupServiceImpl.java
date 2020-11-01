package com.dlq.mall.product.service.impl;

import com.dlq.mall.product.dao.AttrAttrgroupRelationDao;
import com.dlq.mall.product.entity.AttrAttrgroupRelationEntity;
import com.dlq.mall.product.entity.AttrEntity;
import com.dlq.mall.product.service.AttrService;
import com.dlq.mall.product.vo.AttrGrooupRelationVo;
import com.dlq.mall.product.vo.AttrGrooupWithAttrsVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.product.dao.AttrGroupDao;
import com.dlq.mall.product.entity.AttrGroupEntity;
import com.dlq.mall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        //select * pms_attr_group where catelog_id=? and (attr_group_id=key or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public void deleteRelation(AttrGrooupRelationVo[] vos) {

        //relationDao.delete(new QueryWrapper<>().eq("attr_id", 1L).eq("attr_group_id",1L));
        List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(entities);
    }

    /**
     * 根据分类id查出所有的分组以及这些分组的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGrooupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {

        //1、查出分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //2、查出分组下属性信息
        List<AttrGrooupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
            AttrGrooupWithAttrsVo attrGrooupWithAttrsVo = new AttrGrooupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGrooupWithAttrsVo);

            List<AttrEntity> relationAttr = attrService.getRelationAttr(attrGrooupWithAttrsVo.getAttrGroupId());
            attrGrooupWithAttrsVo.setAttrs(relationAttr);
            return attrGrooupWithAttrsVo;
        }).collect(Collectors.toList());

        return collect;
    }

}
