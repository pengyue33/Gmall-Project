package com.atgg.gmall.config;

import com.atgg.gmall.service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:0}")
    private int port;
    @Value("${spring.redis.timeOut:0}")
    private int timeOut;

        @Bean
     public RedisUtil getRedisUtil(){
           if("disabled".equals(host)){
               return null;
           }
           RedisUtil redisUtil = new RedisUtil();
           redisUtil.initJedisPool(host,port,timeOut);
           return  redisUtil;
       }
}
