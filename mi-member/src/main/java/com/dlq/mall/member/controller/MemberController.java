package com.dlq.mall.member.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

import com.dlq.common.exception.BizCodeEnum;
import com.dlq.mall.member.exception.PhoneExistException;
import com.dlq.mall.member.exception.UsernameExistException;
import com.dlq.mall.member.feign.CouponFeignService;
import com.dlq.mall.member.vo.MemLoginVo;
import com.dlq.mall.member.vo.MemRegistVo;
import com.dlq.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dlq.mall.member.entity.MemberEntity;
import com.dlq.mall.member.service.MemberService;
import com.dlq.common.utils.PageUtils;
import com.dlq.common.utils.R;



/**
 * 会员
 *
 * @author dlq
 * @email dlq096@gmail.com
 * @date 2020-10-07 19:54:25
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws IOException, ParseException {
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody MemLoginVo loginVo) {
        MemberEntity entity = memberService.login(loginVo);
        if (entity != null) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemRegistVo registVo){

        try {
            memberService.regist(registVo);
        } catch (PhoneExistException e) {
            e.printStackTrace();
            return R.error(BizCodeEnum.PHONE_EXIT_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIT_EXCEPTION.getMsg());
        }catch (UsernameExistException e){
            e.printStackTrace();
            return R.error(BizCodeEnum.USER_EXIT_EXCEPTION.getCode(), BizCodeEnum.USER_EXIT_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R membercoupons = couponFeignService.membercoupons();

        return R.ok().put("member",memberEntity).put("coupons", membercoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
