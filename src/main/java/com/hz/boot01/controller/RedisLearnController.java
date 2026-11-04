package com.hz.boot01.controller;

import com.hz.boot01.pojo.User;
import com.hz.boot01.utils.JsonMassage;
import com.hz.boot01.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 学习控制器
 *
 * 【设计思路】
 * 这个控制器是 Redis 各种操作的实战示例，
 * 每个接口对应一种 Redis 常用操作，方便你对照学习和测试。
 *
 * 【学习路线】
 * 1. 先看 RedisConfig 了解序列化配置
 * 2. 再看 RedisUtil 了解封装的工具方法
 * 3. 最后看本 Controller 了解实际业务中怎么用
 */
@RestController
@RequestMapping("/redis/learn")
public class RedisLearnController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    // ==================== 1. String 字符串操作 ====================

    /**
     * 存储字符串
     * 访问：GET /redis/learn/setStr?key=name&value=张三
     */
    @GetMapping("/setStr")
    public JsonMassage setStr(String key, String value) {
        redisUtil.setStr(key, value);
        return new JsonMassage(200, "存储成功", 0, null);
    }

    /**
     * 获取字符串
     * 访问：GET /redis/learn/getStr?key=name
     */
    @GetMapping("/getStr")
    public JsonMassage getStr(String key) {
        String value = redisUtil.getStr(key);
        return new JsonMassage(200, "查询成功", 0, value);
    }

    // ==================== 2. 对象缓存操作 ====================

    /**
     * 存储对象（JSON 格式）
     * 【设计思路】
     * 实际开发中，我们经常把数据库查出来的对象缓存到 Redis，
     * 下次直接从 Redis 取，不用再查数据库，性能提升几十倍。
     *
     * 访问：GET /redis/learn/saveUser
     */
    @GetMapping("/saveUser")
    public JsonMassage saveUser() {
        // 模拟从数据库查出来的用户
        User user = new User();
        user.setId(1);
        user.setUserCode("admin");
        user.setUserName("管理员");
        user.setPhone("13800138000");

        // 用 RedisUtil 把对象存到 Redis（自动转 JSON）
        // 第三个参数是过期时间，单位：秒，null 表示永久有效
        redisUtil.setStrJson("user:1", user, 3600L); // 缓存1小时

        return new JsonMassage(200, "用户缓存成功", 0, user);
    }

    /**
     * 获取对象
     * 【缓存模式】Cache-Aside 模式
     * 1. 先查 Redis，命中直接返回
     * 2. Redis 没有，再查数据库
     * 3. 把数据库结果写入 Redis，下次就有了
     *
     * 访问：GET /redis/learn/getUser?userId=1
     */
    @GetMapping("/getUser")
    public JsonMassage getUser(Integer userId) {
        String key = "user:" + userId;

        // 1. 先查 Redis
        User user = (User) redisUtil.getStrJson(key, User.class);

        if (user != null) {
            // Redis 命中，直接返回（不查数据库）
            return new JsonMassage(200, "Redis 缓存命中", 0, user);
        }

        // 2. Redis 没有，模拟查数据库
        user = new User();
        user.setId(userId);
        user.setUserCode("user" + userId);
        user.setUserName("用户" + userId);
        user.setPhone("13900139000");

        // 3. 把数据库结果写入 Redis
        redisUtil.setStrJson(key, user, 3600L);

        return new JsonMassage(200, "数据库查询并缓存", 0, user);
    }

    // ==================== 3. 设置过期时间 ====================

    /**
     * 带过期时间的存储
     * 【应用场景】
     * - 验证码：5分钟过期
     * - 登录 token：7天过期
     * - 秒杀库存：活动结束自动失效
     *
     * 访问：GET /redis/learn/setWithExpire?key=code&value=123456
     */
    @GetMapping("/setWithExpire")
    public JsonMassage setWithExpire(String key, String value) {
        // 60秒后自动过期
        redisUtil.setStr(key, value, 1L); // 第二个参数是分钟，这里是1分钟=60秒
        return new JsonMassage(200, "存储成功，1分钟后过期", 0, null);
    }

    // ==================== 4. 删除缓存 ====================

    /**
     * 删除缓存
     * 【应用场景】
     * - 用户信息修改后，删除旧缓存
     * - 数据更新时，清除对应缓存保证一致性
     *
     * 访问：GET /redis/learn/del?key=user:1
     */
    @GetMapping("/del")
    public JsonMassage del(String key) {
        redisUtil.del(key);
        return new JsonMassage(200, "删除成功", 0, null);
    }

    // ==================== 5. 判断 key 是否存在 ====================

    /**
     * 判断 key 是否存在
     * 【应用场景】
     * - 防止重复提交：判断按钮是否已点击
     * - 分布式锁：判断锁是否已被占用
     *
     * 访问：GET /redis/learn/exists?key=user:1
     */
    @GetMapping("/exists")
    public JsonMassage exists(String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("exists", hasKey);
        return new JsonMassage(200, "查询成功", 0, result);
    }

    // ==================== 6. 分布式锁示例 ====================

    /**
     * Redis 分布式锁示例
     *
     * 【设计思路】
     * 分布式环境下，多个服务实例同时操作同一个资源时，
     * 需要用分布式锁保证同一时间只有一个实例执行。
     *
     * 原理：SETNX（set if not exists），key 不存在才设置成功
     *
     * 访问：GET /redis/learn/tryLock
     */
    @GetMapping("/tryLock")
    public JsonMassage tryLock() {
        String lockKey = "lock:order:123"; // 订单ID=123的锁

        // 尝试加锁，10秒后自动释放（防止死锁）
        boolean locked = redisUtil.lock(lockKey, 10L);

        if (locked) {
            try {
                // 获得锁，执行业务逻辑
                System.out.println("成功获得锁，开始处理订单...");
                // 模拟业务处理
                Thread.sleep(1000);
                return new JsonMassage(200, "加锁成功，业务处理完成", 0, null);
            } catch (Exception e) {
                e.printStackTrace();
                return new JsonMassage(500, "业务处理失败", 0, null);
            } finally {
                // 释放锁
                redisUtil.unLock(lockKey);
                System.out.println("释放锁");
            }
        } else {
            return new JsonMassage(400, "加锁失败，其他任务正在处理", 0, null);
        }
    }
}
