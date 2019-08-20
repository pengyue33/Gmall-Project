package com.atgg.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atgg.gmall.been.*;
import com.atgg.gmall.manage.mapper.*;
import com.atgg.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class ManageServicceImpl implements ManageService {
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired

    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
     @Autowired
     SkuInfoMapper skuInfoMapper;
     @Autowired
     SkuImageMapper skuImageMapper;
     @Autowired
     SkuAttrValueMapper skuAttrValueMapper;
     @Autowired
     SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
             BaseCatalog2 baseCatalog2 = new BaseCatalog2();
             baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
            baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//         return baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoMapper.getAttrList(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
           //添加或修改属性名
        if(baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else{
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
          //清除平台属性值
        BaseAttrValue baseAttrValue1 = new BaseAttrValue();
        baseAttrValue1.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue1);

        //重新为平台属性赋值
        if(baseAttrInfo.getAttrValueList()!=null&&baseAttrInfo.getAttrValueList().size()>0){
            for (BaseAttrValue baseAttrValue : baseAttrInfo.getAttrValueList()) {
                      baseAttrValue.setId(null);
                      baseAttrValue.setAttrId(baseAttrInfo.getId());
                      baseAttrValueMapper.insertSelective(baseAttrValue);
            }

        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        // select * from baseAttrValue where attrId=?
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return    baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        // select * from baseAttrInfo where id = attrId;
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // select * from baseAttrValue where attrId=?baseAttrInfo.getId();
        // 赋值平台属性值集合！
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {

        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }

    /**
     * 保存商品的sup信息
     * @param spuInfo
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
           if(spuInfo.getId()==null||spuInfo.getId().length()==0){
               //保存数据
               spuInfo.setId(null);
               spuInfoMapper.insertSelective(spuInfo);
           }else{
               spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
           }
            //图片先删除在保存
          SpuImage spuImage = new SpuImage();
           spuImage.setSpuId(spuInfo.getId());
           spuImageMapper.delete(spuImage);
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList!=null&&spuImageList.size()>0){
            for (SpuImage image : spuImageList) {
                    image.setId(null);
                    image.setSpuId(spuInfo.getId());
                    spuImageMapper.insertSelective(image);
            }
        }
        // 销售属性 删除，插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);
        // 销售属性值 删除，插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);
           //保存销售属性，
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                 saleAttr.setSpuId(spuInfo.getId());
                 spuSaleAttrMapper.insertSelective(saleAttr);
                      //保存销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                      if(spuSaleAttrValueList!=null&&spuSaleAttrValueList.size()>0){
                          for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                              saleAttrValue.setSpuId(spuInfo.getId());
                              spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                          }
                      }
            }
        }

    }

    @Override
    public List<SpuImage> spuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }

    /**
     * 获取spu的销售属性集合和销售属性值集合
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
       return  spuSaleAttrMapper.spuSaleAttrList(spuId);

    }
    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
         //保存Sku信息
        skuInfoMapper.insertSelective(skuInfo);
        //保存Sku图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList!=null&& skuImageList.size()>0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                  skuImageMapper.insertSelective(skuImage);
            }
        }
        //保存Sku的平台属性值
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                  skuAttrValue.setSkuId(skuInfo.getId());
                  skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
        //保存sku的销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                  skuSaleAttrValue.setSkuId(skuInfo.getId());
                  skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);


            }
        }

    }
}
