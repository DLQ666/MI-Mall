package com.dlq.cart.interceptor;

import com.dlq.cart.vo.UserInfoTo;
import com.dlq.common.constant.AuthServerConstant;
import com.dlq.common.constant.CartConstant;
import com.dlq.common.utils.GetClientIP;
import com.dlq.common.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 *@program: MI-Mall
 *@description: 在执行目标方法之前，判断用户的登录状态  。并封装 传递给controller目标请求
 *@author: Hasee
 *@create: 2021-02-11 20:50
 */
@Slf4j
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();
        String clientIpAddress = GetClientIP.getIpaddr(request);
        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            //用户登录
            userInfoTo.setUserId(member.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //user-key
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }

        //没有临时用户，一定分配一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString().substring(0, 16);
            DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm");
            //格式化
            String str4 = formatter3.format(LocalDateTime.now());

            userInfoTo.setUserKey(uuid + "-" + clientIpAddress + "-" + str4);
        }
        //目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行之后；分配临时用户，让浏览器保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();

        //如果没有临时用户一定保存一个临时用户
        if (!userInfoTo.isTempUser()) {
            //判断请求是否带 临时用户信息，没有就设置临时用户cookie
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("dlqk8s.top");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }

}
