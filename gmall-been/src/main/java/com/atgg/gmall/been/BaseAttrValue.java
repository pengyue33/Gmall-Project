package com.atgg.gmall.been;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
public class BaseAttrValue implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 表示获取主键自增！
    private String id;
    @Column
    private String valueName;
    @Column
    private String attrId;
    @Transient
    private String urlParam;



}
