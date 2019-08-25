package com.atgg.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.SkuInfo;
import com.atgg.gmall.been.SkuLsInfo;
import com.atgg.gmall.service.ListService;
import com.atgg.gmall.service.ManageService;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuManageController {
       @Reference
     private  ManageService manageService;
       @Reference
     private ListService listService ;
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

    /**
     * 商品上架。保存到es中
     * @param skuId
     */
     @RequestMapping("onSale")
    public void onSale(String skuId){
         SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
         SkuLsInfo skuLsInfo = new SkuLsInfo();
         BeanUtils.copyProperties(skuInfo,skuLsInfo);

         listService.saveSkuInfo(skuLsInfo);
     }
}
