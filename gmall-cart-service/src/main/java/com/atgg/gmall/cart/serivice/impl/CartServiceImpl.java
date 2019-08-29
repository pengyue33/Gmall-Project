package com.atgg.gmall.cart.serivice.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atgg.gmall.been.CartInfo;
import com.atgg.gmall.been.SkuInfo;
import com.atgg.gmall.cart.constant.CartConst;
import com.atgg.gmall.cart.mapper.CartInfoMapper;
import com.atgg.gmall.service.CartService;
import com.atgg.gmall.service.ManageService;
import com.atgg.gmall.service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    private ManageService manageService;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 添加购物项到购物车 （一个用户一个购物车 userId）
     * @param skuId
     * @param userId
     * @param i
     */
        @Override
    public void addToCart(String skuId, String userId, int i) {
           //判断购物车中是否有该购物项
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
            if(cartInfoExist!=null){
                //原购物车中存在该商品，数量相加即可
                cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+i);
                // 给实时价格初始化值
                cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
                //将数据更新到数据库中
                cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
            }else{
                // 直接添加到数据库 , 获取skuInfo 信息。添加到cartInfo 中！
                SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
              CartInfo cartInfo1 =   new CartInfo();
                cartInfo1.setSkuId(skuId);
                cartInfo1.setCartPrice(skuInfo.getPrice());
                cartInfo1.setSkuPrice(skuInfo.getPrice());
                cartInfo1.setSkuName(skuInfo.getSkuName());
                cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
                cartInfo1.setUserId(userId);
                cartInfo1.setSkuNum(i);
                //将购物项保存到数据库中
                cartInfoExist=cartInfo1;
                cartInfoMapper.insertSelective(cartInfoExist);
            }
//            放入redis中    （采用 hash 类型）
//            mysql 与 redis 如何进行同步？
//            在添加购物车的时候，直接添加到数据库并添加到redis！
             String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
              //获取redis
            Jedis jedis = redisUtil.getJedis();
               jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));
            // 设置过期时间？跟用户的过期时间一致
            // 获取用户的key
            String userKey =CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
            Long ttl = jedis.ttl(userKey);
             jedis.expire(cartKey,ttl.intValue());
             jedis.close();

        }

    @Override
    public List<CartInfo> getCartList(String userId) {
             /*
            1.  获取jedis
            2.  从redis 中获取数据
            3.  如果有：将redis 数据返回
            4.  如果没有：从数据库查询{查询购物车中的实时价格}，并放入redis
         */
       String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
           //获取所有的value值
        List<String> carInfoListJson = jedis.hvals(cartKey);
           if(carInfoListJson!=null&&carInfoListJson.size()>0){
               for (String cartInfoJson : carInfoListJson) {
                    CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
                    cartInfoList.add(cartInfo);
               }
               // 查询的时候，按照更新的时间倒序！
               cartInfoList.sort(new Comparator<CartInfo>() {
                   @Override
                   public int compare(CartInfo o1, CartInfo o2) {
                       return o1.getId().compareTo(o2.getId());
                   }
               });
               return cartInfoList;
           }else{
            //   4.  如果没有：从数据库查询{查询购物车中的实时价格}，并放入redis
            return    loadCartCache(userId);
           }
    }


    /**
     * 从数据库查询{查询购物车中的实时价格}，并放入redis
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
          //从数据库中查询
        List<CartInfo> cartInfoList=  cartInfoMapper.selectCartListWithCurPrice(userId);
         if(cartInfoList!=null&&cartInfoList.size()>0){
                //保存到redis中
             Jedis jedis = redisUtil.getJedis();
               //购物车key值
             String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
              //定义map集合统一放入redis中
             Map<String,String> map = new HashMap<>();
             for (CartInfo cartInfo : cartInfoList) {
                     map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
             }
              jedis.hmset(cartKey,map);
              return cartInfoList;
         }
         return null;
    }

    /**
     * 合并购物项
     * @param cartListCookie
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCookie, String userId) {
            //从数据库中查询所有购物项
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
           //sukId相同则合并购物项
        for (CartInfo cartInfoCK : cartListCookie) {
            //作为是否合并标志
            boolean flag = true;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                  if(cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                      cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCK.getSkuNum());
                        //更新数据库
                      cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                      flag=false;
                  }
            }
            if(flag){
                  //cookie中购物项在数据库中不存在
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        //完成合并后，最后从数据库查询，并更新缓存
         return loadCartCache(userId);
    }
}
