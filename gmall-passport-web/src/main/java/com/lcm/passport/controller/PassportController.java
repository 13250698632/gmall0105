package com.lcm.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lcm.bean.UmsMember;
import com.lcm.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token){

        //通过jwt校验token真假

        return "success";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember){

        //调用服务验证账号密码
        UmsMember umsMemberLogin = userService.login(umsMember);

        if(umsMemberLogin != null){
            //登录成功
        }else{
            //登录失败
        }

        return "token";
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){
        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";
    }
}
