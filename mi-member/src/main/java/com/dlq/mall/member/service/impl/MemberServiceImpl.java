package com.dlq.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dlq.common.utils.HttpClientUtils;
import com.dlq.mall.member.dao.MemberLevelDao;
import com.dlq.mall.member.entity.MemberLevelEntity;
import com.dlq.mall.member.exception.PhoneExistException;
import com.dlq.mall.member.exception.UsernameExistException;
import com.dlq.mall.member.vo.MemLoginVo;
import com.dlq.mall.member.vo.MemRegistVo;
import com.dlq.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
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

    @Override
    public MemberEntity login(SocialUser socialUser) throws IOException, ParseException {
        MemberDao memberDao = this.baseMapper;
        //登录和注册合并逻辑
        String uid = socialUser.getUid();
        //1、判断当前社交用户是否已经登陆过系统
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null){
            //数据库查出来了------》说明以前登陆过，已经注册
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            memberDao.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;

        }else {
            //没查出来当前社交用户---》说明没登录过---》要新注册
            MemberEntity regist = new MemberEntity();

            try {
                //查询当前社交用户的社交账号信息（昵称，性别等）
                HashMap<String, String> param = new HashMap<>();
                param.put("access_token", socialUser.getAccess_token());
                param.put("uid", socialUser.getUid());
                String baseUrl = "https://api.weibo.com/2/users/show.json";
                HttpClientUtils client = new HttpClientUtils(baseUrl, param);
                client.get();
                if (client.getStatusCode() == 200) {
                    //查询成功
                    String json = client.getContent();
                    JSONObject jsonObject = JSON.parseObject(json);
                    //昵称
                    String name = jsonObject.getString("name");
                    //性别
                    String gender = jsonObject.getString("gender");

                    regist.setNickname(name);
                    regist.setGender("m".equals(gender) ? 1 : 0);
                    //...
                }
            } catch (Exception e) {}
            //差不查询成功--这些都要保存到数据库
            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());

            memberDao.insert(regist);
            return regist;
        }
    }
}
