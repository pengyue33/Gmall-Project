package com.atgg.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atgg.gmall.been.UserAddress;
import com.atgg.gmall.been.UserInfo;
import com.atgg.gmall.service.UserInfoService;
import com.atgg.gmall.user.mapper.UserAddressMapper;
import com.atgg.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class UserInfoServiceImpl implements UserInfoService {
     @Autowired
    UserInfoMapper userInfoMapper;
     @Autowired
    UserAddressMapper userAddressMapper;
    @Override
    public List<UserInfo> getUserInfoList() {

        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
//        Example example = new Example(UserAddress.class);
//        example.createCriteria().andEqualTo("userId",userId);
//        userAddressMapper.selectByExample(example);
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

           return userAddressMapper.select(userAddress);
    }

}
