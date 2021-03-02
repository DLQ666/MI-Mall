package com.dlq.mall.coupon.service.impl;

import com.dlq.mall.coupon.entity.SeckillSkuRelationEntity;
import com.dlq.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.coupon.dao.SeckillSessionDao;
import com.dlq.mall.coupon.entity.SeckillSessionEntity;
import com.dlq.mall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntityList = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationEntities(relationEntityList);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 起始时间
     * @return 起始时间
     */
    private LocalDateTime startTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return start;
    }

    /**
     * 结束时间
     * @return 结束
     */
    private LocalDateTime endTime(){
        LocalDate now = LocalDate.now().plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(now, max);
        end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return end;
    }

}
