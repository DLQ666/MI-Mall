package com.dlq.mall.member.service.impl;

import com.dlq.mall.member.dao.MemberLevelDao;
import com.dlq.mall.member.entity.MemberLevelEntity;
import com.dlq.mall.member.exception.PhoneExistException;
import com.dlq.mall.member.exception.UsernameExistException;
import com.dlq.mall.member.vo.MemLoginVo;
import com.dlq.mall.member.vo.MemRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.Query;

import com.dlq.mall.member.dao.MemberDao;
import com.dlq.mall.member.entity.MemberEntity;
import com.dlq.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemRegistVo registVo) {
        MemberEntity memberEntity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //检查用户名 和 手机号 是否唯一  为了让上层 Controller 感知异常 ，采用异常机制
        checkPhoneUnique(registVo.getPhone());
        checkUsernameUnique(registVo.getUserName());

        //检查用户名和手机是否唯一
        memberEntity.setMobile(registVo.getPhone());
        memberEntity.setUsername(registVo.getUserName());

        //保存密码要采取加密存储
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(registVo.getPassword());
        memberEntity.setPassword(encode);

        //个人中心设置其他信息

        //存入数据库
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer mobile = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile>0){
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException{
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count>0){
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemLoginVo loginVo) {
        String loginacct = loginVo.getLoginacct();
        String password = loginVo.getPassword();
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct)
                .or().eq("mobile", loginacct));
        if (entity == null){
            //查不到  登录失败
            return null;
        }else {
            //如果查到此人----获取到数据库的密码
            String passwordDb = entity.getPassword();
            // 进行密码匹配
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(password, passwordDb);
            if (matches){
                return entity;
            }else {
                return null;
            }
        }
    }
}
