package com.hz.boot01.controller;

import com.hz.boot01.dao.ShardingUserMapper;
import com.hz.boot01.pojo.ShardingUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * ShardingSphere 分表测试接口
 * ============================================================
 *
 * 【完整全链路调用顺序（以添加id=1的用户为例）】
 *
 *  1. 前端发请求：POST http://localhost/sharding/add?id=1&name=张三&age=20
 *  2. Tomcat 接收到请求，DispatcherServlet 分发到这个 Controller 的 add 方法
 *  3. Controller 把参数封装成 ShardingUser 对象
 *  4. 调用 shardingUserMapper.insert(user)
 *  5. MyBatis 代理 Mapper 接口，生成原始 SQL：INSERT INTO user(id, name, age) VALUES(1, '张三', 20)
 *  6. SQL 传给我们配置的 ShardingSphere 包装数据源
 *  7. ShardingSphere 做了这几件事：
 *     - 解析 SQL，识别出这是对逻辑表 user 的插入操作
 *     - 从 SQL 参数里提取分片键的值：id=1
 *     - 执行分表算法：1 % 2 = 1
 *     - 匹配实际节点：路由到 ds0 数据源下的 user_1 表
 *     - 自动把 SQL 改写成：INSERT INTO user_1(id, name, age) VALUES(1, '张三', 20)
 *  8. 改写后的 SQL 发给真正的 MySQL 数据库执行
 *  9. MySQL 返回影响行数 1
 * 10. ShardingSphere 把结果原样返回给 MyBatis，再返回给 Controller
 * 11. Controller 返回 "添加成功！id=1" 给前端
 *
 * 【控制台你会看到两行 SQL（因为我们开了 sql-show: true）】
 * Logic SQL: INSERT INTO user(id, name, age) VALUES(?, ?, ?)  -- 你写的原始SQL
 * Actual SQL: ds0 ::: INSERT INTO user_1(id, name, age) VALUES(?, ?, ?) ::: [1, 张三, 20]  -- 实际执行的SQL
 *
 * 【测试用例】
 * - 传 id=1 → 控制台看到 Actual SQL 里是 user_1（奇数走1号表）
 * - 传 id=2 → 控制台看到 Actual SQL 里是 user_0（偶数走0号表）
 * 这就说明分表路由完全正常工作了！
 * ============================================================
 */
@RestController
@RequestMapping("/sharding")
public class ShardingUserController {

    @Autowired
    private ShardingUserMapper shardingUserMapper;

    /**
     * 添加用户接口
     * 调用方式：POST /sharding/add?id=1&name=张三&age=20
     * 分表路由规则：
     * - id 是奇数（1、3、5...）→ 自动存入 user_1 表
     * - id 是偶数（2、4、6...）→ 自动存入 user_0 表
     */
    @PostMapping("/add")
    public String add(Long id, String name, Integer age) {
        // 1. 封装请求参数为实体对象
        ShardingUser user = new ShardingUser(id, name, age);
        // 2. 调用 Mapper 插入，ShardingSphere 在这里自动做 SQL 改写和路由
        shardingUserMapper.insert(user);
        return "添加成功！id=" + id;
    }

    /**
     * 根据id查询用户
     * 调用方式：GET /sharding/get?id=1
     * ShardingSphere 看到 WHERE id=1，会自动只去 user_1 表查，不用两张表都扫
     */
    @GetMapping("/get")
    public ShardingUser get(Long id) {
        return shardingUserMapper.selectById(id);
    }

    /**
     * 查询所有用户
     * 调用方式：GET /sharding/list
     * ShardingSphere 会同时查 user_0 和 user_1 两张表，然后把结果合并成一个 List 返回
     */
    @GetMapping("/list")
    public List<ShardingUser> list() {
        return shardingUserMapper.selectAll();
    }
}
