package com.atgg.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.*;
import com.atgg.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin
public class ManageController {
      @Reference
     private ManageService manageService;
     @RequestMapping("index")
    public String gotoIndex(){
         return "index";
     }

    /**
     * 一级分类集合
     */
    @RequestMapping(value = "getCatalog1"  )
    @ResponseBody
     public List<BaseCatalog1> getCatalog1(){

        return   manageService.getCatalog1();
     }

    /**
     * 获得二级分类集合
     *
     * @param catalog1Id
     * @return
     */
     @RequestMapping(value = "getCatalog2")
     @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id ){
         List<BaseCatalog2> catalog2 = manageService.getCatalog2(catalog1Id);
         return catalog2;
     }

    /**
     * 获得三级分类集合
     * @param catalog2Id
     * @return
     */
    @RequestMapping(value = "getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id ){
        List<BaseCatalog3> catalog3 = manageService.getCatalog3(catalog2Id);
        return catalog3;
    }

    /**
     * 获得平台属性
     * @param catalog3Id
     * @return
     */
    @RequestMapping(value = "attrInfoList")
    @ResponseBody
    //http://localhost:8082/attrInfoList?catalog3Id=61
   public List<BaseAttrInfo> attrInfoList(String catalog3Id ){
       List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
         return attrList;
   }

    /**
     * 保存or修改平台属性和平台属性值
     * @param baseAttrInfo
     */
   @RequestMapping("saveAttrInfo")
   @ResponseBody
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
   }

    /**
     * 获取平台属性值的回显数据
     * @return
     */
   @RequestMapping("getAttrValueList")
   @ResponseBody
   public List<BaseAttrValue> getAttrValueList(String attrId){
         return   manageService.getAttrInfo(attrId).getAttrValueList();
      }
}
