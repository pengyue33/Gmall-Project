package com.atgg.gmall.service;

import com.atgg.gmall.been.SkuLsInfo;
import com.atgg.gmall.been.SkuLsParams;
import com.atgg.gmall.been.SkuLsResult;

public interface ListService {
     public void saveSkuInfo(SkuLsInfo skuLsInfo);

     public SkuLsResult search(SkuLsParams skuLsParams);
}
