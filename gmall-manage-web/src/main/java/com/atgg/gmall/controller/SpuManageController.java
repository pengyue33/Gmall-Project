package com.atgg.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.BaseSaleAttr;
import com.atgg.gmall.been.SpuInfo;
import com.atgg.gmall.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class SpuManageController {
           @Reference
    ManageService manageService;
           //spu列表
          @RequestMapping("spuList")
    public List<SpuInfo>  getSupInfoList(String catalog3Id){
              SpuInfo spuInfo = new SpuInfo();
              spuInfo.setCatalog3Id(catalog3Id);
             return manageService.getSpuInfoList(spuInfo);
          }
          //baseSaleAttrList
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){

        return   manageService.getBaseSaleAttrList();
    }
   @RequestMapping("saveSpuInfo")
    public String saveSpuInfo( @RequestBody SpuInfo spuInfo){
       manageService.saveSpuInfo(spuInfo);
       return "OK";
   }
}
