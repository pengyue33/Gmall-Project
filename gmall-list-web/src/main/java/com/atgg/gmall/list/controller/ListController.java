package com.atgg.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.SkuLsParams;
import com.atgg.gmall.been.SkuLsResult;
import com.atgg.gmall.service.ListService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListController {
       @Reference
    private ListService listService ;
      @RequestMapping("list.html")
    public String getList(SkuLsParams  skuLsParams){
          SkuLsResult search = listService.search(skuLsParams);
          String resultString = JSON.toJSONString(search);
          return  resultString;
      }
}
