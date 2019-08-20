package com.atgg.gmall.manage.mapper;


import com.atgg.gmall.been.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
     List<BaseAttrInfo> getAttrList(String catalog3Id);
}
