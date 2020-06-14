package com.example.user.controller;

import com.example.user.bean.UmsMember;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){

        List<UmsMember> umsMemberList = userService.getAllUser();

        return umsMemberList;
    }

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "首页显示";
    }
}
