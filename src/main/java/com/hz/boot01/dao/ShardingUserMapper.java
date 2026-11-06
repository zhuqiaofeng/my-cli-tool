package com.hz.boot01.dao;

import com.hz.boot01.pojo.ShardingUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ============================================================
 * 分表用户 Mapper 数据访问层
 * ============================================================
 *
 * 【注意！这里写的表名是 user，不是 user_0 或 user_1！】
 * 这就是 ShardingSphere 最强大的地方：
 * 你完全不用关心分表细节，就像操作普通单表一样写 SQL。
 *
 * 【SQL 改写过程（ShardingSphere 自动做的）】
 * 1. 你写的 SQL：INSERT INTO user(id, name, age) VALUES(1, '张三', 20)
 * 2. ShardingSphere 拦截 SQL，提取到分片键 id=1
 * 3. 计算 1 % 2 = 1 → 应该路由到 user_1 表
 * 4. 自动把 SQL 改写成：INSERT INTO user_1(id, name, age) VALUES(1, '张三', 20)
 * 5. 把改写后的 SQL 发给真正的 MySQL 执行
 * 6. 执行完把结果原样返回，你根本不知道中间发生了改写
 *
 * 【查询的时候更智能】
 * - 如果你 WHERE id=1 查询：ShardingSphere 知道1是奇数，只去查 user_1 表就行，不用全查
 * - 如果你不带 id 条件查所有：ShardingSphere 会同时查 user_0 和 user_1 两张表，然后把结果合并成一个 List 返回给你
 * ============================================================
 */
public interface ShardingUserMapper {

    /**
     * 添加用户
     * 你写的是操作 user 表，ShardingSphere 会根据 id 自动路由
     */
    @Insert("INSERT INTO user(id, name, age) VALUES(#{id}, #{name}, #{age})")
    void insert(ShardingUser user);

    /**
     * 根据id查询用户
     * ShardingSphere 看到 WHERE id=?，会自动只查对应的那一张表，不会全表扫描
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    ShardingUser selectById(Long id);

    /**
     * 查询所有用户
     * ShardingSphere 会同时查 user_0 和 user_1，然后合并结果
     */
    @Select("SELECT * FROM user")
    List<ShardingUser> selectAll();
}
