package com.atgg.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.UserAddress;
import com.atgg.gmall.been.UserInfo;
import com.atgg.gmall.service.UserInfoService;
import com.atgg.gmall.service.util.RedisUtil;
import com.atgg.gmall.user.mapper.UserAddressMapper;
import com.atgg.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class UserInfoServiceImpl implements UserInfoService {

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;
      @Autowired
      RedisUtil redisUtil;

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

    /**
     * 认证用户登录信息
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
         //密码加密后比较
        String passd = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(passd);
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfo);
        if(userInfo1!=null){
            // 获得到redis ,将用户存储到redis中
            Jedis jedis=null;
            try {
                jedis= redisUtil.getJedis();
                jedis.setex(userKey_prefix+userInfo1.getId()+userinfoKey_suffix,userKey_timeOut,
                        JSON.toJSONString(userInfo1));

            } finally {
                if(jedis!=null){
                    jedis.close();
                }

            }
            return userInfo1;
        }
        return null;
    }

    /**
     * 认证用户是否登录
     * @param userId
     * @return
     */
    @Override
    public UserInfo verify(String userId) {
          //定义key值
        String key =userKey_prefix+userId+userinfoKey_suffix;
        Jedis jedis = redisUtil.getJedis();
             //从redis中获取用户信息
        String userJson = jedis.get(key);
        UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
          if(userInfo!=null){
                //延长redis中存储时间
              jedis.expire(key,userKey_timeOut);
               jedis.close();
               return userInfo;
          }
        return null;
    }

}
