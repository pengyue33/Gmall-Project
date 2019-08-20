package com.atgg.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.SkuInfo;
import com.atgg.gmall.service.ManageService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuManageController {
       @Reference
    private  ManageService manageService;
    /**
     * 保存Sku信息
     * @return
     */

    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
         if(skuInfo!=null){
             manageService.saveSkuInfo(skuInfo);
         }
         return "OK";
     }
}
