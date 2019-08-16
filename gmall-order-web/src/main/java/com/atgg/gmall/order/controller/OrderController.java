package com.atgg.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.UserAddress;
import com.atgg.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {
      //@Autowired
    @Reference
    UserInfoService userInfoService;
    @RequestMapping("trade")
    @ResponseBody // 第一个返回json 字符串，fastJson.jar 第二直接将数据显示到页面！
    public List<UserAddress> trade(String userId){
        // 返回一个视图名称叫index.html
        return userInfoService.getUserAddressList(userId);
    }



}
