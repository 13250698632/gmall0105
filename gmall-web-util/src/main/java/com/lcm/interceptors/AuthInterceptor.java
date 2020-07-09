package com.lcm.interceptors;

import com.lcm.annotations.LoginRequired;
import com.lcm.util.CookieUtil;
import com.lcm.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //拦截代码

        //判断拦截的请求的访问方法的注解
        HandlerMethod method = (HandlerMethod) handler;
        //获取注解
        LoginRequired methodAnnotation = method.getMethodAnnotation(LoginRequired.class);

        //是否拦截
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        //获得该请求是否必登录成功
        boolean loginSuccess = methodAnnotation.loginSuccess();

        //调认证中心进行验证
        String success = "fail";
        if(StringUtils.isNotBlank(token)) {
            success = HttpclientUtil.doGet("http://127.0.0.1:8086/verify?token=" + token);
        }

        if (loginSuccess) {
            //必须登录成功才能使用
            if (!success.equals("success")) {
                //重定向回passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://127.0.0.1:8086/index?ReturnUrl="+requestURL);
                return false;
            }
            //验证通过,覆盖cookie中的token
            request.setAttribute("memberId", "1");
            request.setAttribute("nickname", "nickname");
            if(StringUtils.isNotBlank(token)){
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
            }

        } else {
            //没有登录也能用，但是必须验证
            if (success.equals("success")) {
                //需要将token携带的用户信息写入
                request.setAttribute("memberId", "1");
                request.setAttribute("nickname", "nickname");
                //验证通过,覆盖cookie中的token
                if(StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
        }

        return true;
    }
}
