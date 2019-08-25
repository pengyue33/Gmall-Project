package com.atgg.gmall.manage.mapper;

import com.atgg.gmall.been.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
