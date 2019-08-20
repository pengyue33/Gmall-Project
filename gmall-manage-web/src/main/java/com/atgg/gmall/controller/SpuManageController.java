package com.atgg.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.BaseSaleAttr;
import com.atgg.gmall.been.SpuImage;
import com.atgg.gmall.been.SpuInfo;
import com.atgg.gmall.been.SpuSaleAttr;
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
     //获取Spu图片集合
   @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(SpuImage spuImage){
         return   manageService.spuImageList(spuImage);
   }
    //获取spu的销售属性集合和销售属性值集合
    //http://localhost:8082/spuSaleAttrList?spuId=60
     @RequestMapping("spuSaleAttrList")
    public  List<SpuSaleAttr> spuSaleAttrList(String spuId){
       return   manageService.spuSaleAttrList(spuId);
    }
}
