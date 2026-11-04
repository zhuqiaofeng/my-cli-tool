# Spring Boot + Redis 最小运行模块 学习指南

> 本模块为学习目的设计，包含完整的 Redis 集成示例和详细注释，方便你对照学习。

---

## 一、模块结构说明

```
src/main/java/com/hz/boot01/
├── config/
│   └── RedisConfig.java        # Redis 配置类（序列化配置）
├── controller/
│   ├── RedisLearnController.java  # Redis 操作学习控制器
│   └── ProviderController.java    # 原有业务控制器
├── service/
│   ├── UserCacheService.java     # 用户缓存服务（业务层示例）
│   └── ProviderService.java      # 原有业务服务
├── utils/
│   ├── RedisUtil.java            # Redis 工具类（封装常用操作）
│   └── JsonMassage.java          # 统一返回结果类
└── pojo/
    └── User.java                 # 用户实体类
```

---

## 二、学习路线（建议按顺序学习）

### 第 1 步：理解 RedisConfig 配置类
**文件位置**：`config/RedisConfig.java`

**学习要点**：
- 为什么要自定义 RedisTemplate？
  - 默认 JDK 序列化：乱码、体积大、不可读
  - 自定义序列化：key 用 String，value 用 JSON，可读性好

- 两种序列化方式的区别：
  | 序列化方式 | 优点 | 缺点 | 使用场景 |
  |---|---|---|---|
  | StringRedisSerializer | 字符串可读、轻量 | 只能存字符串 | key、简单字符串 value |
  | Jackson2JsonRedisSerializer | 对象转 JSON、跨语言 | 有一定性能损耗 | 复杂对象 value |

### 第 2 步：熟悉 RedisUtil 工具类
**文件位置**：`utils/RedisUtil.java`

**封装的常用方法**：
| 方法 | 作用 | 应用场景 |
|---|---|---|
| `setStr(key, value)` | 存储字符串 | 存验证码、token |
| `getStr(key)` | 获取字符串 | 读取验证码 |
| `setStrJson(key, obj, time)` | 存储对象（JSON） | 缓存用户信息 |
| `getStrJson(key, Class)` | 获取对象 | 读取缓存对象 |
| `del(key)` | 删除缓存 | 数据更新后清缓存 |
| `lock(key, expire)` | 加分布式锁 | 防止并发重复操作 |
| `unLock(key)` | 释放锁 | 业务完成后解锁 |

### 第 3 步：看 Controller 学习实际调用
**文件位置**：`controller/RedisLearnController.java`

**可测试的接口**：
```
GET /redis/learn/setStr?key=name&value=张三    # 存字符串
GET /redis/learn/getStr?key=name              # 取字符串
GET /redis/learn/saveUser                     # 存用户对象
GET /redis/learn/getUser?userId=1             # 取用户对象（带缓存）
GET /redis/learn/setWithExpire?key=code&value=123456  # 带过期时间
GET /redis/learn/del?key=user:1               # 删除缓存
GET /redis/learn/exists?key=user:1            # 判断 key 是否存在
GET /redis/learn/tryLock                      # 分布式锁示例
```

### 第 4 步：深入业务层缓存模式
**文件位置**：`service/UserCacheService.java`

**核心模式：Cache-Aside 模式（旁路缓存）**

```
读请求：
  应用 -> Redis -> 命中？-> 返回
                |
                未命中
                |
                数据库 -> 写入 Redis -> 返回

写请求：
  应用 -> 更新数据库 -> 删除 Redis 缓存
```

**为什么写操作是删除缓存而不是更新缓存？**
1. 并发安全：写数据库和写缓存不是原子操作，可能导致不一致
2. 懒加载：删除后下次查询自动加载最新数据
3. 性能：避免频繁更新可能不被读取的缓存

---

## 三、Redis 常见使用场景

### 1. 缓存热点数据
- 场景：商品详情、用户信息、配置数据
- 优点：大幅减少数据库查询压力
- 关键：设置合理的过期时间，避免缓存雪崩

### 2. 分布式锁
- 场景：秒杀防超卖、订单防重复提交
- 原理：SETNX（key 不存在才设置成功）
- 注意：设置过期时间，防止死锁

### 3. 验证码 / 短信码
- 场景：登录验证码、支付验证码
- 过期时间：一般 5 分钟
- 验证后立即删除

### 4. 登录 Token
- 场景：用户登录状态保持
- 过期时间：7 天 / 30 天
- 支持续期

### 5. 排行榜
- 场景：销量排行榜、积分排行榜
- 数据结构：ZSet（有序集合）

---

## 四、常见问题与面试点

### Q1：缓存穿透怎么办？
**问题**：大量查询不存在的 key，每次都打到数据库
**解决**：
- 缓存空值（设置短过期时间）
- 布隆过滤器

### Q2：缓存雪崩怎么办？
**问题**：大量 key 同时过期，瞬间请求全部打到数据库
**解决**：
- 过期时间加随机值，避免同时失效
- 热点数据不过期，异步更新

### Q3：缓存击穿怎么办？
**问题**：某个热点 key 过期瞬间，大量并发请求打到数据库
**解决**：
- 分布式锁，只允许一个请求查数据库
- 热点数据永不过期

### Q4：缓存和数据库一致性怎么保证？
**常用策略**：
1. 先更新数据库，再删除缓存
2. 设置合理的过期时间
3. 重要数据用延迟双删

---

## 五、配置说明

确保 `application.yml` 中有 Redis 配置：

```yaml
spring:
  redis:
    host: 127.0.0.1      # Redis 服务器地址
    port: 6379            # 端口
    password:             # 密码（没有就留空）
    database: 0           # 数据库编号
    timeout: 3000ms       # 超时时间
```

---

## 六、启动测试步骤

1. 确保本地 Redis 服务已启动
2. 启动 Spring Boot 项目
3. 用浏览器或 Postman 访问上面的测试接口
4. 用 Redis 客户端（如 RedisDesktopManager）查看存储的数据

**观察重点**：
- 第一次调用 `getUser?userId=1`：日志显示"缓存未命中"
- 第二次调用同样的接口：日志显示"缓存命中"
- 在 Redis 客户端里可以看到 `user:1` 这个 key
