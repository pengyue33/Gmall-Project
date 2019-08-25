package com.atgg.gmall.been;

import lombok.Data;

import java.io.Serializable;

@Data  //es查询封装条件参数类
public class SkuLsParams implements Serializable{
    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;

}
