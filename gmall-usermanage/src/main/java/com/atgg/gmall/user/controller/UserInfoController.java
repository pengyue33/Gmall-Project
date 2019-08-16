package com.atgg.gmall.user.controller;

import com.atgg.gmall.been.UserInfo;
import com.atgg.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoController {
     @Autowired
     private UserInfoService userInfoService;
     @RequestMapping("findAll")
     @ResponseBody
    public List<UserInfo> findAll(){
        List<UserInfo> userInfoList = userInfoService.getUserInfoList();
        return userInfoList;
    }

}
