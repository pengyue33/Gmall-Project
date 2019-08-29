package com.atgg.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.SkuInfo;
import com.atgg.gmall.been.SkuSaleAttrValue;
import com.atgg.gmall.been.SpuSaleAttr;
import com.atgg.gmall.been.SpuSaleAttrValue;
import com.atgg.gmall.config.LoginRequire;
import com.atgg.gmall.service.ListService;
import com.atgg.gmall.service.ManageService;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
        @Reference
       private ManageService  manageService;
        @Reference
       private ListService listService;
        //自定义注解测试
     //@LoginRequire(autoRedirect = true)
     @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable String skuId, HttpServletRequest request){
         SkuInfo skuInfo =manageService.getSkuInfoBySkuId(skuId);
          //根据Id获取Sku的基本信息
         request.setAttribute("skuInfo",skuInfo);
           //获取spu销售属性集合 ，用来回显，及默认选中Sku值
      List<SpuSaleAttr>  saleAttrList  =manageService.getSpuSaleAttrListCheckBySku(skuInfo);
         request.setAttribute("saleAttrList",saleAttrList);
         //通过销售属性值，确定具体的Sku,转成json字符串，传到前端进行
        List<SkuSaleAttrValue>  skuSaleAttrValueList =manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
         // 118|120 = 33 119|121=34 118|122=35 组成json 字符串！
         // map.put("118|120",33 ) 然后将转换为json字符串即可！
               String key ="";
         Map<String, String > map = new HashMap<>();

         for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
             SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
             if(key.length()>0){
                 key+="|";
             }
              key+=skuSaleAttrValue.getSaleAttrValueId();
                  //最后一个sku 或者 skuId 不相等的两个，key不拼接
             if(i+1==skuSaleAttrValueList.size()||! skuSaleAttrValueList.get(i+1).getSkuId().equals(skuSaleAttrValue.getSkuId())){
                   map.put(key,skuSaleAttrValue.getSkuId());
                  // 清空key值；
                 key="";
             }
         }
         //将map转化为json字符串，返回到前台
         String jStr = JSON.toJSONString(map);
       //  {"122|125":"35","122|124":"34","121|123":"33"}
         request.setAttribute("valuesSkuJson",jStr);

            //更新热度评分
         listService.incrHotScore(skuId);
          return "item";
     }

}
