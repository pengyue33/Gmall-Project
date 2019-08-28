package com.atgg.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atgg.gmall.been.*;
import com.atgg.gmall.manage.constant.ManageConst;
import com.atgg.gmall.manage.mapper.*;
import com.atgg.gmall.service.ManageService;
import com.atgg.gmall.service.util.RedisUtil;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServicceImpl implements ManageService {
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired

    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
     @Autowired
     SkuInfoMapper skuInfoMapper;
     @Autowired
     SkuImageMapper skuImageMapper;
     @Autowired
     SkuAttrValueMapper skuAttrValueMapper;
     @Autowired
     SkuSaleAttrValueMapper skuSaleAttrValueMapper;
     @Autowired
    RedisUtil redisUtil;


    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
             BaseCatalog2 baseCatalog2 = new BaseCatalog2();
             baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
            baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//         return baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoMapper.getAttrList(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
           //添加或修改属性名
        if(baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else{
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
          //清除平台属性值
        BaseAttrValue baseAttrValue1 = new BaseAttrValue();
        baseAttrValue1.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue1);

        //重新为平台属性赋值
        if(baseAttrInfo.getAttrValueList()!=null&&baseAttrInfo.getAttrValueList().size()>0){
            for (BaseAttrValue baseAttrValue : baseAttrInfo.getAttrValueList()) {
                      baseAttrValue.setId(null);
                      baseAttrValue.setAttrId(baseAttrInfo.getId());
                      baseAttrValueMapper.insertSelective(baseAttrValue);
            }

        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        // select * from baseAttrValue where attrId=?
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return    baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        // select * from baseAttrInfo where id = attrId;
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // select * from baseAttrValue where attrId=?baseAttrInfo.getId();
        // 赋值平台属性值集合！
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {

        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }

    /**
     * 保存商品的sup信息
     * @param spuInfo
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
           if(spuInfo.getId()==null||spuInfo.getId().length()==0){
               //保存数据
               spuInfo.setId(null);
               spuInfoMapper.insertSelective(spuInfo);
           }else{
               spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
           }
            //图片先删除在保存
          SpuImage spuImage = new SpuImage();
           spuImage.setSpuId(spuInfo.getId());
           spuImageMapper.delete(spuImage);
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList!=null&&spuImageList.size()>0){
            for (SpuImage image : spuImageList) {
                    image.setId(null);
                    image.setSpuId(spuInfo.getId());
                    spuImageMapper.insertSelective(image);
            }
        }
        // 销售属性 删除，插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);
        // 销售属性值 删除，插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);
           //保存销售属性，
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                 saleAttr.setSpuId(spuInfo.getId());
                 spuSaleAttrMapper.insertSelective(saleAttr);
                      //保存销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                      if(spuSaleAttrValueList!=null&&spuSaleAttrValueList.size()>0){
                          for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                              saleAttrValue.setSpuId(spuInfo.getId());
                              spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                          }
                      }
            }
        }

    }

    @Override
    public List<SpuImage> spuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }

    /**
     * 获取spu的销售属性集合和销售属性值集合
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
       return  spuSaleAttrMapper.spuSaleAttrList(spuId);

    }
    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
         //保存Sku信息
        skuInfoMapper.insertSelective(skuInfo);
        //保存Sku图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList!=null&& skuImageList.size()>0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                  skuImageMapper.insertSelective(skuImage);
            }
        }
        //保存Sku的平台属性值
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                  skuAttrValue.setSkuId(skuInfo.getId());
                  skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
        //保存sku的销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                  skuSaleAttrValue.setSkuId(skuInfo.getId());
                  skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);


            }
        }

    }

    /**
     * 根据Id 获取Sku信息
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {
        return getSkuInfoByRedissonLock(skuId);
    }
         //通过redisson框架解分布锁问题
    private SkuInfo getSkuInfoByRedissonLock(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis =null;
        RLock rLock=null;
        try {
            //SkuInfoKey
            String skuInfoKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            //获取jedis
            jedis = redisUtil.getJedis();
            if (jedis.exists(skuInfoKey)) {
                String skuInfoJsonStr = jedis.get(skuInfoKey);
                //从缓存中获取数据
                if (!StringUtil.isEmpty(skuInfoJsonStr)) {
                    skuInfo = JSON.parseObject(skuInfoJsonStr, SkuInfo.class);
                    return skuInfo;
                }
            } else {
                //获取redisson锁
                Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.2.132:6379");
                RedissonClient redissonClient = Redisson.create(config);
                //锁
                rLock = redissonClient.getLock("My_Lock");
                rLock.lock(10, TimeUnit.SECONDS);

                //从数据库中获取数据
                skuInfo = getSkuInfo(skuId);
                //放入缓存中
                String skuInfoStr = JSON.toJSONString(skuInfo);
                //设置过期时间
                jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, skuInfoStr);
                return skuInfo;
            }
        }catch(Exception e){
                e.printStackTrace();
            }

         finally {
             if(jedis!=null){
                 jedis.close();
             }
             if(rLock!= null){
                 rLock.unlock();
             }
        }
        // 从db走！
        return getSkuInfo(skuId);
    }

    //通过redis加分布锁解决 缓存击穿问题
    private SkuInfo getSkuInfoByRedisLock(String skuId) {
        SkuInfo skuInfo =null;
        Jedis jedis =null;
        try {
             jedis = redisUtil.getJedis();
             String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
             String skuInfoJson = jedis.get(skuInfoKey);
             if(skuInfoJson==null){ //缓存中没有数据
                  //使用redis分布锁，防止缓存击穿
                  //定义锁key 值
                 String  lockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                 String result = jedis.set(lockKey, "666", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                 if("OK".equals(result)){
                       //获取分布式锁
                     skuInfo = getSkuInfo(skuId);
                     String skuJsonString = JSON.toJSONString(skuInfo);
                     //将数据存到redis中
                     jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,skuJsonString);
                       //删除掉锁
                     jedis.del(lockKey);
                     return skuInfo;
                 }else{
                     //等待
                     Thread.sleep(1000);
                     return getSkuInfo(skuId);
                 }

             }else{
                skuInfo= JSON.parseObject(skuInfoJson,SkuInfo.class);
                  return skuInfo;
             }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }

        }
        return getSkuInfo(skuId);
    }

    //redis 宕机解决方法
    private SkuInfo getSkuInfoForRedisDown(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis=null;
        //redis中Skuinfo 的键
        String SkuInfoKey= ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
        //try_catch_finally 解决redis宕机情况
        try {
            jedis = redisUtil.getJedis();
            if(jedis.exists(SkuInfoKey)){
                   //从redis中获取信息
                String skuInfoValue = jedis.get(SkuInfoKey);
                     if(skuInfoValue!=null&&skuInfoValue.length()>0){
                         skuInfo = JSON.parseObject(skuInfoValue,SkuInfo.class);
                     }
                return skuInfo;
            }else {
                 //从数据库中查询SkuInfo
                skuInfo= getSkuInfo(skuId);
                 //将查询后数据，存储到Redis中
                String jsonString = JSON.toJSONString(skuInfo);
                jedis.set(SkuInfoKey,jsonString);
                return skuInfo;
            }
        } catch (Exception e) {
          //  System.out.println("catch____________________________");
            e.printStackTrace();
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        // System.out.println("zuihou____________________________");
        //catch捕获异常后,下面的代码可以执行
        return getSkuInfo(skuId);
    }

    //从数据库中获取SkuInfo信息
    private SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if(skuInfo!=null){
            SkuImage skuImage = new SkuImage();
            skuImage.setSkuId(skuId);
              //图片
            List<SkuImage> skuImages = skuImageMapper.select(skuImage);
            skuInfo.setSkuImageList(skuImages);
            SkuAttrValue skuAttrValue = new SkuAttrValue();
             //平台属性值集合
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
            skuInfo.setSkuAttrValueList(skuAttrValueList);
        }
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        String skuInfoId = skuInfo.getId();
        String spuId = skuInfo.getSpuId();

        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuInfoId,spuId);
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {

        return skuSaleAttrValueMapper.getSkuSaleAttrValueListBySpu(spuId);
    }
    /**
     * 通过平台属性值Id查询平台属性集合
     * @param attrValueIdList
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        List<BaseAttrInfo>  baseAttrInfoList=   baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
         return baseAttrInfoList;
    }
}
