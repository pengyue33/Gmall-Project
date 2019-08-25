package com.atgg.gmall.been;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data  //封装es返回结果的类
public class SkuLsResult implements Serializable{

    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;

}
