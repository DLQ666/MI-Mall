package com.dlq.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.dlq.auth.feign.MemberFeignService;
import com.dlq.auth.feign.SmsFeignService;
import com.dlq.auth.vo.UserLoginVo;
import com.dlq.auth.vo.UserRegistVo;
import com.dlq.common.constant.AuthServerConstant;
import com.dlq.common.exception.BizCodeEnum;
import com.dlq.common.utils.R;
import com.dlq.common.utils.RandomUtils;
import com.dlq.common.vo.MemberRespVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-02-06 20:21
 */
@Controller
public class LoginController {

    @Autowired
    SmsFeignService smsFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memFeignService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        System.out.println(phone);
        if (StringUtils.isEmpty(phone)){
            return R.error(BizCodeEnum.PHONE_NULL_EXCEPTION.getCode(),BizCodeEnum.PHONE_NULL_EXCEPTION.getMsg());
        }else {
            //TODO 1、接口防刷

            String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
            if (!StringUtils.isEmpty(redisCode)) {
                long redisGetTime = Long.parseLong(redisCode.split("_")[1]);
                if (System.currentTimeMillis() - redisGetTime < 60 * 1000 * 5) {
                    //60s 内不能再发送
                    return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
                }
            }

            String code = RandomUtils.getSixBitRandom();
            //2、验证码再次校验-->  存 key-->phone，value->code   sms:code:12312341234 -> 123444
            String subString = code + "_" + System.currentTimeMillis();
            //redis缓存验证码 防止同一个phone在60s内 再次发送验证码
            redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, subString, 2, TimeUnit.HOURS);

            smsFeignService.sendCode(phone, code);
            return R.ok();
        }
    }

    /**
     * / / ToDo 重定向携带数据，利用session原理。将数据放在session中。
     * // 只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     *
     * //ToDo 1、分布式下的session间题。
     * RedirectAttributes redirectAttributes:模拟重定向携带数据
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            /**
             * // 原始
             * List<Map<String, String>> collect = result.getFieldErrors().stream().map(fieldError -> {
             *                 String field = fieldError.getField();
             *                 String defaultMessage = fieldError.getDefaultMessage();
             *                 errors.put(field, defaultMessage);
             *                 return errors;
             *             }).collect(Collectors.toList());
             *  //升级版  收集为Map
             *  Map<String, String> collect = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
             *                 return fieldError.getField();
             *             }, fieldError -> {
             *                 return fieldError.getDefaultMessage();
             *             }));
             *  // 再升级 下面方法引用
             */
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            //model.addAttribute("errors",errors);
            //校验出错 转发到注册页
            //Request method 'POST' not supported
            //用户注册->/regist[post]----》转发/reg.html（路径映射默认都是get方式访问的。)
            //return "forward:/regist.html";

            //模拟重定向 携带数据
            redirectAttributes.addFlashAttribute("errors", errors);
            //采用直接渲染
            return "redirect:http://auth.dlqk8s.top:81/regist.html";
        }

        //所有校验通过 调用远程服务进行真正注册
        //1、校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isEmpty(s)){
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            //采用直接渲染
            return "redirect:http://auth.dlqk8s.top:81/regist.html";
        }else {
            if (code.equals(s.split("_")[0])){
                //删除验证码；令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码通过---->调用远程服务进行注册
                R r = memFeignService.regist(vo);
                if (r.getCode() == 0){
                    //成功 //注册成功回到首页，回到登录页
                    return "redirect:http://auth.dlqk8s.top:81/login.html";
                }else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.dlqk8s.top:81/regist.html";
                }
            }else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                //采用直接渲染
                return "redirect:http://auth.dlqk8s.top:81/regist.html";
            }
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession httpSession){
        Object attribute = httpSession.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null){
            //没登录
            return "login";
        }else {
            return "redirect:http://dlqk8s.top:81/";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo,RedirectAttributes redirectAttributes, HttpSession session){

        //远程调用登录服务
        R login = memFeignService.login(vo);
        if (login.getCode() == 0){
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);

            //成功
            return "redirect:http://dlqk8s.top:81/";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            //采用直接渲染
            return "redirect:http://auth.dlqk8s.top:81/login.html";
        }
    }
}
