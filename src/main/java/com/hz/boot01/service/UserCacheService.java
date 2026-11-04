package com.hz.boot01.service;

import com.hz.boot01.pojo.User;
import com.hz.boot01.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户缓存服务
 *
 * 【设计思路】
 * 这是一个完整的业务层缓存示例，展示实际开发中
 * 如何用 Redis 优化数据库查询性能。
 *
 * 【缓存模式：Cache-Aside 模式（最常用）】
 * 读流程：
 *   1. 先查 Redis
 *   2. 命中 -> 直接返回
 *   3. 未命中 -> 查数据库 -> 写入 Redis -> 返回
 *
 * 写流程：
 *   1. 更新数据库
 *   2. 删除对应 Redis 缓存（下次查询时会重新加载）
 *
 * 【为什么写操作是删除缓存而不是更新缓存？】
 * 1. 避免并发问题：写数据库和写缓存不是原子操作
 * 2. 懒加载：删除后，下次查询时自动从数据库加载最新数据
 * 3. 减少缓存污染：有些数据可能写入后很少被读取
 */
@Service
public class UserCacheService {

    @Autowired
    private RedisUtil redisUtil;

    // Redis key 前缀，规范管理
    private static final String USER_CACHE_PREFIX = "user:";

    // 缓存过期时间：1小时
    private static final long USER_CACHE_EXPIRE = 3600L;

    /**
     * 根据 ID 查询用户（带缓存）
     *
     * 【缓存流程】
     * 请求进来 -> 查 Redis -> 命中？-> 返回
     *                   |
     *                   未命中
     *                   |
     *                   查数据库（模拟）
     *                   |
     *                   写入 Redis
     *                   |
     *                   返回
     */
    public User getUserById(Integer userId) {
        String key = USER_CACHE_PREFIX + userId;

        // 1. 先查 Redis
        User user = redisUtil.getStrJson(key, User.class);
        if (user != null) {
            System.out.println("【缓存命中】从 Redis 读取用户：" + userId);
            return user;
        }

        // 2. 缓存未命中，模拟查数据库
        System.out.println("【缓存未命中】从数据库查询用户：" + userId);
        user = getUserFromDB(userId);

        // 3. 写入 Redis，下次查询就有了
        if (user != null) {
            redisUtil.setStrJson(key, user, USER_CACHE_EXPIRE);
        }

        return user;
    }

    /**
     * 更新用户信息
     *
     * 【缓存策略】
     * 更新数据库后，删除对应缓存，而不是更新缓存。
     * 这样下次查询时会自动从数据库加载最新数据，
     * 避免并发场景下的数据不一致问题。
     */
    public void updateUser(User user) {
        // 1. 更新数据库（模拟）
        System.out.println("【数据库】更新用户信息：" + user.getId());

        // 2. 删除缓存（重要！）
        String key = USER_CACHE_PREFIX + user.getId();
        redisUtil.del(key);
        System.out.println("【缓存】删除用户缓存：" + key);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Integer userId) {
        // 1. 删除数据库（模拟）
        System.out.println("【数据库】删除用户：" + userId);

        // 2. 删除缓存
        String key = USER_CACHE_PREFIX + userId;
        redisUtil.del(key);
        System.out.println("【缓存】删除用户缓存：" + key);
    }

    /**
     * 模拟从数据库查询用户
     * 实际项目中这里会调用 DAO 层查询数据库
     */
    private User getUserFromDB(Integer userId) {
        // 模拟数据库查询延迟
        try {
            Thread.sleep(100); // 模拟数据库查询耗时
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User user = new User();
        user.setId(userId);
        user.setUserCode("user" + userId);
        user.setUserName("测试用户" + userId);
        user.setPhone("138****" + String.format("%04d", userId));
        user.setAddress("北京市朝阳区");
        return user;
    }
}
