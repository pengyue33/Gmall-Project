package com.atgg.gmall.manage.mapper;

import com.atgg.gmall.been.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
       List<SpuSaleAttr> spuSaleAttrList(String spuId) ;
}
