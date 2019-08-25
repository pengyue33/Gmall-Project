package com.atgg.gmall.service;

import com.atgg.gmall.been.*;

import java.util.List;

public interface ManageService {
    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(String attrId);
    public BaseAttrInfo getAttrInfo(String attrId);

    /**
     * 获取Spu 列表
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    // 查询基本销售属性表
    List<BaseSaleAttr> getBaseSaleAttrList();

    // 接口保存Spu相关信息
    public void saveSpuInfo(SpuInfo spuInfo);


    List<SpuImage> spuImageList(SpuImage spuImage);

    List<SpuSaleAttr> spuSaleAttrList(String spuId);

    /**
     * 保存Sku 相关信息
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据id获取SkuIfo信息
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoBySkuId(String skuId);

    /**
     * 获取Spu的销售属性及被选中的sku
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 通过销售属性返回所有匹配的sku集合
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
