package com.hz.boot01.utils;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================
 * Redis 工具类：封装常用的 Redis 操作
 * ============================================================
 *
 * 【Redis 是什么？】
 * Redis 是一个基于内存的 Key-Value 数据库，读写速度极快（微秒级），
 * 通常用来做缓存、分布式锁、计数器、消息队列等。
 *
 * 【这里用 StringRedisTemplate 的好处】
 * 直接存字符串，在 Redis 客户端里能直接看到存的内容，方便学习和调试，
 * 不会像默认 RedisTemplate 那样存成 JDK 序列化的乱码。
 *
 * 【对象存取的流程】
 * 存对象：Java对象 → FastJSON转成JSON字符串 → 存到Redis
 * 取对象：从Redis读JSON字符串 → FastJSON反序列化成Java对象
 *
 * 【完整调用顺序（以getUser接口为例）】
 * 1. 前端请求 /redis/learn/getUser?userId=1
 * 2. Controller 调用 redisUtil.getStrJson("user:1", User.class)
 * 3. RedisUtil 从 Redis 里读 key=user:1 的值（是个JSON字符串）
 * 4. FastJSON 把 JSON 字符串反序列化成 User 对象
 * 5. 返回给 Controller，Controller 返回给前端
 * ============================================================
 */
@Component
public class RedisUtil {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * ValueOperations 是专门操作 String 类型数据的工具类
     * Spring 会自动注入，不用我们自己 new
     */
    @Resource(name = "stringRedisTemplate")
    ValueOperations<String, String> valOpsStr;

    /**
     * 从 Redis 读取 JSON 并转成 Java 对象
     * @param key Redis 键
     * @param clazz 要转成的 Java 类型（比如 User.class）
     * @return 反序列化后的对象，key不存在时返回null
     */
    public <T> T getStrJson(String key, Class<T> clazz){
        // 1. 先从 Redis 里拿到原始的 JSON 字符串
        String json = valOpsStr.get(key);
        if (json == null) {
            return null;
        }
        // 2. 用 FastJSON 把 JSON 字符串转成对应的 Java 对象
        return JSON.parseObject(json, clazz);
    }

    /**
     * 把 Java 对象转成 JSON 字符串存到 Redis
     * @param key Redis 键
     * @param object 要存的 Java 对象
     * @param time 过期时间（秒），传 null 表示永久有效
     */
    //time 秒
    public void setStrJson(String key, Object object, Long time){
        // 1. 用 FastJSON 把 Java 对象转成 JSON 字符串
        String json = JSON.toJSONString(object);
        // 2. 存到 Redis，如果传了过期时间就设置自动过期
        if(time != null){
            valOpsStr.set(key, json, time, TimeUnit.SECONDS);
        }else{
            valOpsStr.set(key, json);
        }
    }


    /**
     * 根据key读取字符串值
     * @param key Redis 键
     * @return 字符串值
     */
    public String getStr(String key){
        return valOpsStr.get(key);
    }

    /**
     * 存字符串，永久有效
     * @param key Redis 键
     * @param value 字符串值
     */
    public void setStr(String key, String value){
        valOpsStr.set(key, value);
    }

    /**
     * 存字符串，带过期时间
     * @param key Redis 键
     * @param value 字符串值
     * @param expire 过期时间（分钟）
     */
    public void setStr(String key, String value, Long expire){
        valOpsStr.set(key, value, expire, TimeUnit.MINUTES);
    }

    /**
     * 删除指定的 key
     * 应用场景：数据更新后删除旧缓存，下次查询会重新从数据库读并存入缓存
     * @param key 要删除的 Redis 键
     */
    public void del(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * 加分布式锁（Spring Boot 3 推荐写法）
     *
     * 【为什么要用 setIfAbsent？】
     * 这就是 Redis 分布式锁的核心：只有 key 不存在的时候才能设置成功
     * 相当于 "如果这个锁没人占，我就占上；如果已经有人占了，就加锁失败"
     *
     * 【为什么要设置过期时间？】
     * 防止业务程序宕机了，锁永远不释放（死锁）
     * 到时间 Redis 会自动删除这个 key，其他程序就能拿到锁
     *
     * @param key 锁的标识（比如 lock:order:123 表示锁定123号订单）
     * @param expire 锁自动过期时间（秒）
     * @return true=加锁成功，false=锁已被其他程序占用
     */
    public boolean lock(String key, Long expire){
        // setIfAbsent 就是 SETNX（set if not exists）的新版写法，Spring Boot 3 已经废弃了旧的 setNX 方法
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放分布式锁
     * 业务执行完（不管成功失败）都要在 finally 里调用这个方法，把锁删掉，让其他程序能用
     * @param key 要释放的锁的标识
     */
    public void unLock(String key){
        stringRedisTemplate.delete(key);
    }
}
