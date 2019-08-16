package com.atgg.gmall.service;

import com.atgg.gmall.been.UserAddress;
import com.atgg.gmall.been.UserInfo;

import java.util.List;

public interface UserInfoService {
    List<UserInfo> getUserInfoList();
    public List<UserAddress> getUserAddressList(String userId);
}
