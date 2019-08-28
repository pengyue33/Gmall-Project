package com.atgg.gmall.manage.mapper;


import com.atgg.gmall.been.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
     List<BaseAttrInfo> getAttrList(String catalog3Id);

     List<BaseAttrInfo> selectAttrInfoListByIds(@Param("attrValueIds") String attrValueIds);
}
