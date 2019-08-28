package com.atgg.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.*;
import com.atgg.gmall.service.ListService;

import com.atgg.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Controller
public class ListController {
      @Reference
    private ListService listService ;
      @Reference
    private ManageService manageService;
       @RequestMapping("list.html")
    public String getList(SkuLsParams  skuLsParams,Model model){
           // 设置每页显示的条数
           skuLsParams.setPageSize(2);
          SkuLsResult search = listService.search(skuLsParams);
           // 获取skuLsInfo 集合
          List<SkuLsInfo> skuLsInfoList = search.getSkuLsInfoList();
          model.addAttribute("skuLsInfoList",skuLsInfoList);
          //显示平台属性，平台属性值
           List<BaseAttrInfo> baseAttrInfoList=null;
            if(skuLsParams.getCatalog3Id()!=null&&skuLsParams.getCatalog3Id().length()>0){
                baseAttrInfoList  = manageService.getAttrList(skuLsParams.getCatalog3Id());
            }else{
                List<String> attrValueIdList = search.getAttrValueIdList();
                baseAttrInfoList = manageService.getAttrList(attrValueIdList);
            }

           // 编写一个方法记录当前的查询条件：
           String urlParam = makeUrlParam(skuLsParams);
           model.addAttribute("urlParam",urlParam);

            // 声明一个面包屑集合
           ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();
           // 获取到集合中平台属性值Id
           for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                 BaseAttrInfo baseAttrInfo =  iterator.next();
               List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
               for (BaseAttrValue baseAttrValue : attrValueList) {
                        //获取条件中valueId
                     if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                         for (String valueId : skuLsParams.getValueId()) {
                                 if(valueId.equals(baseAttrValue.getId())){
                                     // 删除平台属性对象
                                      iterator.remove();
                                     // 构成面包屑：baseAttrValueed 的名称就是面包屑
                                     BaseAttrValue baseAttrValue1 = new BaseAttrValue();
                                     baseAttrValue1.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                                         //重构请求路径。自己本身的属性值去掉。
                                     String newUrl  =  makeUrlParam(skuLsParams,valueId);
                                      baseAttrValue1.setUrlParam(newUrl);
                                     //将baseAttrValue1放入面包屑集合中
                                     baseAttrValueArrayList.add(baseAttrValue1);
                                 }
                         }
                     }
               }

           }
            //保存面包屑
           model.addAttribute("baseAttrValueArrayList",baseAttrValueArrayList);
            //保存keyword
           model.addAttribute("keyword",   skuLsParams.getKeyword());
             //分页
              //总页数
           model.addAttribute("totalPages",search.getTotalPages());
              //当前页
           model.addAttribute("pageNo",skuLsParams.getPageNo());
           //平台台属性，平台属性值
           model.addAttribute("baseAttrInfoList",baseAttrInfoList);

            return  "list";
      }

    /**
     * 记录查询条件的方法
     * @param valueIds 面包屑删除传入valueId
     * @param skuLsParams
     * @return
     */
    private String makeUrlParam(SkuLsParams skuLsParams,String...valueIds) {
         String urlParam="";
        // 判断keyword  urlParam = keyword=skuLsParams.getKeyword()
        if(skuLsParams.getKeyword()!=null&&skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }

           // 三级分类Id  keyword=skuLsParams.getKeyword()&
            if(skuLsParams.getCatalog3Id()!=null&&skuLsParams.getCatalog3Id().length()>0){
                if(urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
            }

           // href="list.html?keyword=?&valueId=?"
           // 平台属性值Id
            if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                for (String valueId : skuLsParams.getValueId()) {
                    if(valueIds!=null && valueIds.length>0){
                        //存在，去除本身valueId，不做拼接，完成面包屑的删除功能
                        String id = valueIds[0];
                        if(valueId.equals(id)){
                             continue; //跳出当次循环
                        }
                    }
                     if(urlParam.length()>0){
                         urlParam+="&";
                     }
                    urlParam+="valueId="+valueId;
                }
            }


         return urlParam;
    }
}
