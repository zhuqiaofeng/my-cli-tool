# Spring Boot 3.2.5 后端中间件实战学习笔记

> 本文档基于 `boot01` 项目实战整理，覆盖从版本升级到常用中间件集成的全流程，包含核心原理、代码结构、数据流转和踩坑记录。
>
> **技术栈版本**：Spring Boot 3.2.5 / JDK 21 / MySQL 8.0 / MyBatis 3.5 / Redis 7.x / RabbitMQ 3.7 / MongoDB 7.0 / Elasticsearch 8.13 / ShardingSphere 5.5.1

---

## 📦 第一部分：项目环境与版本升级

### 1.1 版本升级历程
从 Spring Boot 2.1.7 + JDK 1.8 升级到 Spring Boot 3.2.5 + JDK 21，核心改动：
1. **包名替换**：所有 `javax.*` 包替换为 `jakarta.*`（Spring Boot 3 强制要求）
2. **依赖升级**：
   - MyBatis：3.5.x → 适配 Spring Boot 3.0.3 版本
   - MySQL 驱动：mysql-connector-java → mysql-connector-j 8.3.0
   - Fastjson → Fastjson2 2.0.32
3. **配置适配**：
   - CORS：`allowedOrigins("*")` 报错，改用 `allowedOriginPatterns("*")`
   - Redis：`setNX()` 废弃，改用 `opsForValue().setIfAbsent()`
   - JDK 21 不再内置 JAXB API，需要手动引入依赖

### 1.2 C盘迁移原则
- 所有新软件默认安装到 D 盘，不占用 C 盘空间
- **避开系统保护目录**：`C:\Program Files`、`D:\Program Files` 普通权限无法修改文件，软件安装到 `D:\Tools` 或 `D:\Program Files\非系统保护目录`
- IDEA 缓存/配置迁移到 `D:\IDEAData`，释放 C 盘 800+MB 空间

---

## 🗄️ 第二部分：各中间件模块详解

---

### 2.1 Redis 缓存
#### 核心原理
Redis 是基于内存的 Key-Value 数据库，读写性能微秒级，常用于：
- 热点数据缓存（Cache-Aside 模式）
- 分布式锁
- 验证码/Token 过期管理
- 计数器/限流

#### 数据流转（缓存查询）
```
请求 → 先查Redis → 命中直接返回
                ↓ 未命中
              查MySQL数据库 → 结果写入Redis（设置过期时间）→ 返回
```

#### 关键代码位置
| 文件 | 作用 |
|------|------|
| `config/RedisConfig.java` | 自定义 RedisTemplate，配置 Jackson JSON 序列化，避免乱码 |
| `utils/RedisUtil.java` | 封装字符串存取、对象JSON转换、分布式锁方法 |
| `controller/RedisLearnController.java` | 各种 Redis 操作示例接口 |

#### 踩坑记录
1. **默认序列化乱码**：Spring Boot 默认 JDK 序列化存的 key/value 都是乱码，必须自定义 String+Jackson 序列化
   - 解决：key 用 StringRedisSerializer，value 用 Jackson2JsonRedisSerializer，在 RedisConfig 里手动配置 RedisTemplate
2. **ObjectMapper配置坑**：Jackson序列化默认只识别public字段，实体里的private字段序列化会丢失
   - 解决：设置 `objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)`，让Jackson识别所有修饰符的字段
3. **分布式锁API变更**：Spring Boot 3 废弃了 `setNX()`，必须用 `setIfAbsent()`
   - 解决：改用 `opsForValue().setIfAbsent(key, value, timeout, unit)`，原子性设置过期时间
4. **锁必须设置过期时间**：防止程序宕机导致死锁，Redis 会自动过期释放
   - 注意：生产环境不要直接 `delete(key)` 释放锁，要先判断锁的value是不是自己设置的（防止删了别人的锁），学习阶段简化了
5. **缓存模式注意事项**：
   - 缓存穿透：查不存在的key每次都打数据库，要加空值缓存或者布隆过滤器
   - 缓存雪崩：大量key同时过期，要给过期时间加随机偏移
   - 双写一致性：更新数据库后要及时删除旧缓存，不要先更新缓存再更新数据库
6. **StringRedisTemplate vs RedisTemplate区别**：
   - StringRedisTemplate：key和value都是String，存进去直接可读，学习和调试最方便
   - RedisTemplate：默认JDK序列化，存出来是乱码，必须自定义序列化才能用
7. **序列化空值问题**：从Redis读key的时候，如果key不存在返回null，不要直接强转成对象，会报空指针
   - 解决：getStrJson方法里先判断json是否为null，再做反序列化

---

### 2.2 RabbitMQ 消息队列
#### 核心概念
```
生产者(Producer) → 交换机(Exchange) → 队列(Queue) → 消费者(Consumer)
```
- **交换机**：消息分发中心，生产者不直接发队列，全部发给交换机
- **路由键(Routing Key)**：交换机根据这个值决定把消息发给哪个队列
- **绑定(Binding)**：交换机和队列之间的映射关系
- **Direct交换机**：路由键完全匹配才转发，类似点对点通信

#### 数据流转（发送邮件消息为例）
```
1. MqController.sendEmail() 调用 rabbitTemplate.convertAndSend()
2. 消息发到 exchange.direct 交换机
3. 交换机匹配路由键 routing.email
4. 找到绑定了该key的 queue.email 队列
5. 队列有消息后自动推送给 EmailMqConsumer
6. Consumer 监听方法自动执行，打印/处理消息
```

#### 关键代码位置
| 文件 | 作用 |
|------|------|
| `config/RabbitMQConfig.java` | 声明队列、交换机、绑定关系 |
| `controller/MqController.java` | 消息生产者，发送消息接口 |
| `service/HelloMqConsumer.java` | hello队列消费者 |
| `service/EmailMqConsumer.java` | email队列消费者 |

#### 踩坑记录
1. **未启动MQ服务器导致启动报错**：引入依赖但没启动 RabbitMQ 时，需要排除自动配置或禁用注解，否则项目启动就会一直刷连接失败日志
2. **路由键不匹配导致消息丢失**：交换机发消息时路由键和绑定的路由键必须完全一致，Direct交换机是精确匹配，写错一个字符消息就会被丢弃
3. **队列持久化**：声明队列时durable=true，RabbitMQ重启后队列和消息不会丢失，不设置的话重启就全没了
4. **消息消费者幂等性**：实际业务里可能重复消费同一条消息，要做幂等处理（比如用消息ID去重）

---

### 2.3 Quartz 定时任务
#### 核心概念
- **Job**：任务内容（你要执行的逻辑）
- **Trigger**：触发规则（多久执行一次、什么时候执行）
- **Scheduler**：调度器，负责把Job和Trigger组装起来定时执行

#### 数据流转
```
项目启动 → Scheduler初始化 → 按Cron表达式到点自动执行Job → 执行完成等待下次触发
```

#### 关键代码位置
| 文件 | 作用 |
|------|------|
| `config/QuartzConfig.java` | 配置Job和Trigger |
| `job/HelloJob.java` | 定时任务业务逻辑 |

#### 踩坑记录
1. **repeatCount计数规则**：repeatCount=2 表示总共执行3次（包含第一次），不是执行2次
2. **启动执行时机**：任务默认在项目启动后立刻执行一次，不是等第一个周期到了才执行

---

### 2.4 MongoDB 文档数据库
#### 核心概念
- 面向文档存储，类似JSON，不需要固定表结构
- Spring Data MongoDB 自动生成 Repository 实现，不用写SQL
- 适合存评论、日志、商品详情等结构不固定的数据

#### 数据流转（新增评论为例）
```
1. CommentController 接收请求
2. 调用 CommentRepository.save(comment)
3. Spring Data 自动生成MongoDB插入语句
4. 数据存入 test 库的 comment 集合
```

#### 关键代码位置
| 文件 | 作用 |
|------|------|
| `pojo/Comment.java` | 评论实体，@Document 标识MongoDB文档 |
| `repository/CommentRepository.java` | 数据访问层，继承MongoRepository |
| `controller/CommentController.java` | 评论CRUD接口 |

#### 踩坑记录
1. **MapperScan扫描冲突**：MyBatis的@MapperScan会误扫MongoDB Repository导致Bean冲突
   - 解决：把MongoDB/ES的Repository放到独立的`repository`包，和MyBatis的`dao`包分开
2. **启动时自动连接Mongo**：只要引入依赖，项目启动就会尝试连接Mongo，没启动Mongo服务会直接报错
   - 学习阶段不用的时候临时注释依赖，或者先启动Mongo服务
3. **日志打印SQL**：配置 `logging.level.org.springframework.data.mongodb.core.MongoTemplate: DEBUG` 可以在控制台看到MongoDB执行的语句，调试很方便
4. **@Document注解作用**：标识这个类对应MongoDB里的哪个集合，不写的话默认用类名首字母小写当集合名

---

### 2.5 Elasticsearch 搜索引擎
#### 核心概念
- 倒排索引：把文档内容拆成词，记录每个词出现在哪些文档里，查询时直接找包含关键词的文档
- **Text字段**：会被分词，适合做全文搜索
- **Keyword字段**：不分词，适合精确匹配、排序

#### 数据流转（文章搜索为例）
```
1. ArticleController 接收请求
2. 调用 ArticleRepository 方法
3. Spring Data ES 自动生成DSL查询语句
4. ES在article索引里做倒排索引查询
5. 返回匹配的文章列表
```

#### 关键代码位置
| 文件 | 作用 |
|------|------|
| `pojo/Article.java` | 文章实体，@Document 标识ES索引 |
| `repository/ArticleRepository.java` | ES数据访问层，自动生成查询 |
| `controller/ArticleController.java` | 文章CRUD接口 |

#### 踩坑记录
1. **ik分词器未安装报错**：实体里配置了ik_max_word分词器，但本地ES没装插件会启动失败，学习阶段用默认标准分词器即可，等需要中文分词的时候再装ik插件
2. **LocalDateTime格式不匹配**：ES存储时间格式和Java的LocalDateTime不兼容，查询时会报格式转换错误
   - 解决：学习demo里直接把时间字段改成String类型最省事，不用配置复杂的时间转换器
3. **版本兼容问题**：Spring Boot 3.2.5自带的Spring Data ES 5.2.x只支持ES 8.x，不支持ES 7.x，版本要对应
4. **安全认证关闭**：本地学习时把ES的xpack.security.enabled设为false，不用输用户名密码就能连接，省很多事

---

### 2.6 ShardingSphere 分库分表
#### 核心概念
- **逻辑表**：你代码里写的表名（比如user），实际数据库里不存在
- **物理表**：真实存在的分表（user_0、user_1）
- **分片键**：按哪个字段的值决定路由到哪个表（这里用id）
- **路由规则**：`id % 2 = 0 → user_0`，`id % 2 = 1 → user_1`

#### 数据流转（插入id=1的用户为例）
```
1. Controller 调用 mapper.insert(user)
2. MyBatis 生成原始SQL：INSERT INTO user(id,name,age) VALUES(1,'张三',20)
3. ShardingSphere 拦截SQL，提取分片键 id=1
4. 计算 1%2=1 → 路由到 user_1 表
5. 自动改写SQL：INSERT INTO user_1(id,name,age) VALUES(1,'张三',20)
6. 发给真实MySQL执行，结果原样返回给业务代码
```

#### 关键代码位置
| 文件 | 作用 |
|------|------|
| `resources/sharding.yml` | 分表规则配置 |
| `pojo/ShardingUser.java` | 分表用户实体 |
| `dao/ShardingUserMapper.java` | 分表数据访问层 |
| `controller/ShardingUserController.java` | 分表测试接口 |

#### 踩坑记录（重点！版本死锁）
1. **5.4.1版本死锁**：ShardingSphere 5.4.1 需要 snakeyaml 1.x，Spring Boot 3.2.5 需要 snakeyaml 2.x，两个版本不兼容，降级任何一个都会导致另一个启动失败
2. **解决方案**：直接升级到 ShardingSphere 5.5.1+，这个版本原生适配 Spring Boot 3.x 和 snakeyaml 2.x，没有版本冲突
3. **Java 21 JAXB问题**：老版本需要手动加JAXB依赖，5.5.1已经自动适配Jakarta XML Bind

---

### 2.7 XXL-Job 分布式任务调度
- 执行器代码已经写好，调度中心需要单独部署（Java项目+MySQL表）
- 学习阶段暂时注释掉执行器配置，避免后台一直刷连接错误日志

---

## 📂 第三部分：项目分层结构总结
```
boot01/
├── config/          # 各种中间件配置类
│   ├── RedisConfig.java
│   ├── RabbitMQConfig.java
│   ├── QuartzConfig.java
│   └── ShardingSphereConfig.java
├── controller/      # 接口层，接收请求
│   ├── MqController.java
│   ├── RedisLearnController.java
│   ├── CommentController.java
│   ├── ArticleController.java
│   └── ShardingUserController.java
├── service/         # 业务逻辑层，MQ消费者在这里
│   ├── HelloMqConsumer.java
│   └── EmailMqConsumer.java
├── dao/             # MyBatis Mapper接口
│   └── ShardingUserMapper.java
├── repository/      # Spring Data 自动Repository（MongoDB/ES）
│   ├── CommentRepository.java
│   └── ArticleRepository.java
├── pojo/            # 实体类
├── job/             # Quartz任务类
├── utils/           # 工具类（RedisUtil等）
└── resources/
    ├── application.yml  # 主配置文件
    └── sharding.yml     # ShardingSphere分表配置
```

---

## 🚨 第四部分：启动问题排查清单
| 报错现象 | 原因 | 解决方法 |
|----------|------|----------|
| Table 'smbms.user' doesn't exist | ShardingSphere没生效，直接查逻辑表 | 检查数据源是否配置成ShardingSphere驱动 |
| TagInspector ClassNotFound | snakeyaml版本不对 | 不要手动降级snakeyaml，用Spring Boot默认2.x版本 |
| Representer构造方法找不到 | ShardingSphere 5.4.1和snakeyaml 2.x不兼容 | 升级到5.5.1+版本 |
| Multiple Spring Data modules found | 同时引入MongoDB/ES/Redis，Repository扫描冲突 | 不同中间件的Repository放不同包 |
| 端口80被占用 | 其他程序占了80端口 | 改server.port或者杀掉占用进程 |
| 连接被拒绝 | 对应中间件服务没启动 | 先启动RabbitMQ/MongoDB/ES再启动项目 |
| Command line is too long | IDEA依赖太多，命令行超长 | Run Configurations里Shorten command line选JAR manifest |

---

## 🎯 学习路径建议
1. 先搞懂Spring Boot自动配置原理，理解为什么加依赖就能用
2. 逐个测试每个中间件的接口，对照控制台打印的日志，把注释里的流程和实际运行对应起来
3. 重点理解每个中间件解决了什么问题：
   - Redis：解决数据库读压力大的问题
   - RabbitMQ：解决系统解耦、异步调用的问题
   - MongoDB：解决非结构化数据存储的问题
   - Elasticsearch：解决全文搜索慢的问题
   - ShardingSphere：解决单表数据量太大查询慢的问题
   - Quartz/XXL-Job：解决定时任务自动执行的问题
