package com.hz.boot01.utils;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Resource(name = "stringRedisTemplate")
    ValueOperations<String, String> valOpsStr;

    @Resource(name = "redisTemplate")
    ValueOperations<Object, Object> valOpsObj;

    public <T> T getStrJson(String key, Class<T> clazz){
        String j = valOpsStr.get(key);
        return JSON.parseObject(j, clazz);
    }

    //time 秒
    public void setStrJson(String key,Object object,Long time){
        String json = JSON.toJSONString(object);
        if(time!=null){
            valOpsStr.set(key,json,time,TimeUnit.SECONDS);
        }else{
            valOpsStr.set(key,json);
        }
    }


    /**
     * 根据指定key获取String
     * @param key
     * @return
     */
    public String getStr(String key){

        return valOpsStr.get(key);
    }

    /**
     * 设置Str缓存
     * @param key
     * @param val
     */
    public void setStr(String key, String val){
        valOpsStr.set(key,val);
    }

    /***
     * 设置Str缓存
     * @param key
     * @param val
     * @param expire 超时时间
     */
    public void setStr(String key, String val,Long expire){
        valOpsStr.set(key,val,expire, TimeUnit.MINUTES);
    }

    /**
     * 删除指定key
     * @param key
     */
    public void del(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * 根据指定o获取Object
     * @param o
     * @return
     */
    public Object getObj(Object o){

        return valOpsObj.get(o);
    }

    /**
     * 设置obj缓存
     * @param o1
     * @param o2
     */
    public void setObj(Object o1, Object o2){
        valOpsObj.set(o1, o2);
    }

    /**
     * 删除Obj缓存
     * @param o
     */
    public void delObj(Object o){
        redisTemplate.delete(o);
    }








    /***
     * 加锁的方法（Spring Boot 3 推荐写法，用 setIfAbsent 代替 deprecated 的 setNX）
     * @param key 锁的 key
     * @param expire 过期时间（秒）
     * @return 是否加锁成功
     */
    public boolean lock(String key, Long expire){
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }
    /***
     * 解锁的方法
     * @param key 锁的 key
     */
    public void unLock(String key){
        stringRedisTemplate.delete(key);
    }
}
