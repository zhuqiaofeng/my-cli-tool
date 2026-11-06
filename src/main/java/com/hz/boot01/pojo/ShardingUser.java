package com.hz.boot01.pojo;

/**
 * ============================================================
 * ShardingSphere 分表示例：用户实体
 * ============================================================
 *
 * 【这个实体神奇在哪？】
 * 你在代码里操作的是一个叫 user 的"逻辑表"，但 MySQL 里根本没有这张表！
 * MySQL 里真实存在的是 user_0 和 user_1 两张结构完全一样的物理表。
 *
 * 【分表规则（我们在sharding.yml里配置的）】
 * - id 是偶数（比如2、4、6）→ 数据存在 user_0 表
 * - id 是奇数（比如1、3、5）→ 数据存在 user_1 表
 * 路由公式：实际表名 = user_${id % 2}
 *
 * 【对你完全透明是什么意思？】
 * 你写代码的时候，根本不知道背后有分表！
 * 你就像操作一张普通表一样写 SQL，ShardingSphere 在中间帮你自动：
 * 1. 解析你的 SQL，提取 id 值
 * 2. 按公式计算应该路由到哪张表
 * 3. 自动把 SQL 里的表名改成真实的物理表名
 * 4. 执行完把结果原样返回给你
 * 整个过程你完全感知不到，就像只有一张表一样。
 *
 * 【什么时候需要分表？】
 * 当单表数据量超过千万级，查询性能下降的时候，
 * 把一张大表按某个规则拆成多张小表，分散存储，提升查询速度。
 * ============================================================
 */
public class ShardingUser {
    /**
     * 分片键：分表就是按这个字段的值来决定路由到哪个表
     * 这里我们用 id 作为分片键，按 id 取模分表
     */
    private Long id;

    private String name;

    private Integer age;

    public ShardingUser() {}

    public ShardingUser(Long id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
