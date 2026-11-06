# SpringBoot3 自动配置原理详解

>
> 适配项目：`com.hz.boot01.Boot01Application`，JDK21 + SpringBoot 3.2.5
> 配套：项目代码内详细注释，包含注解解析、底层组件、完整调用链路、数据流转、踩坑记录

## 一、核心入口：启动类

```
@SpringBootApplication
public class Boot01Application {
    public static void main(String[] args) {
        SpringApplication.run(Boot01Application.class, args);
    }
}
```

`@SpringBootApplication` 是**复合注解**，等价下面三个注解组合：

```
@Configuration          // 标记当前类为配置类，允许 @Bean 注册组件
@EnableAutoConfiguration // ✨【自动配置核心】开启SpringBoot自动装配
@ComponentScan          // 组件扫描：扫描当前包以及子包所有 @Component/@Service/@Controller
public @interface SpringBootApplication {}
```

### 1.1 @ComponentScan 组件扫描

- 默认扫描规则：**启动类所在包以及所有子包**，本项目：`com.hz.boot01.*`
- 项目效果：自动识别 `@Controller`、`@Service`、`@Repository`、`@Component`>
> ⚠️踩坑：Controller / Service 如果放在启动类包外面，不会被扫描，抛出 `NoSuchBeanDefinitionException`

### 1.2 @Configuration 配置类

- 启动类本身也是配置类，`SpringApplication.run()` 会把启动类纳入Spring上下文。
- 项目中 `*Config.java` 全部使用该注解，用来手动注册第三方中间件 Bean（Redis、RabbitMQ、ShardingSphere等）。

### 1.3 ✨@EnableAutoConfiguration 自动配置开关

```
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {}
```

>
> 核心：导入 `AutoConfigurationImportSelector` 选择器，这是自动装配的灵魂。

## 二、AutoConfigurationImportSelector 自动配置选择器

### 底层资源文件位置

>
> SpringBoot2 和 SpringBoot3 文件位置发生变更

- SpringBoot2：`META‑INF/spring.factories`
- SpringBoot3：`spring-boot-autoconfigure-3.2.5.jar/META‑INF/spring/org.springframework.boot.autoconfigure.imports`

文件存放所有场景自动配置类全限定名，本项目会加载：

```
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
……
```

### 工作流程

1. `AutoConfigurationImportSelector` 读取 `org.springframework.boot.autoconfigure.imports`，获取自动配置类全类名列表
2. 通过**条件注解 `@ConditionalXXX`**过滤配置类，判断是否需要实例化
3. 将满足条件的配置类导入Spring容器，创建Bean对象

### 核心条件注解

| 注解 | 作用 |
| --- | --- |
| `@ConditionalOnClass` | classpath存在指定类才生效，例如classpath存在`RedisOperations`才开启Redis自动配置 |
| `@ConditionalOnMissingBean` | 容器**不存在该Bean才创建**，用于允许用户自定义Bean覆盖默认自动配置 |
| `@ConditionalOnProperty` | application.yml存在对应配置项才生效 |
| `@ConditionalOnWebApplication` | Web Servlet环境下才生效 |

>
> 📌项目实践：RedisConfig自定义RedisTemplate
> 自动配置的RedisTemplate上加了`@ConditionalOnMissingBean(RedisTemplate.class)`，我们手动在RedisConfig声明`@Bean RedisTemplate`，自动配置就不会再创建默认实例，实现自定义覆盖默认行为。

## 三、SpringApplication.run() 完整调用链路 & 数据流转

入口代码：`SpringApplication.run(Boot01Application.class, args);`
分为两大阶段：**构造SpringApplication对象 → run()执行完整启动流程**

### 时序链路

```
main()
└── SpringApplication.run(主类, args)
    ├── ① new SpringApplication() 构造器初始化
    │   ├── 设置资源加载器
    │   ├── 推断应用类型（SERVLET/web / REACTIVE响应式 / NONE普通Java程序）【本项目:SERVLET】
    │   ├── 读取META‑INF/spring.factories，加载 ApplicationContextInitializer 初始化器
    │   ├── 读取META‑INF/spring.factories，加载 ApplicationListener 事件监听器
    │   ├── 推断main主启动类
    └── ② run() 核心启动逻辑
        ├── Step1: StopWatch计时器，统计项目启动耗时（控制台日志：Started Boot01Application in 6.335 seconds）
        ├── Step2: SpringApplicationRunListeners，发布生命周期事件
        │        └── starting()：项目即将启动事件
        ├── Step3: 解析处理命令行参数 ApplicationArguments
        ├── Step4: prepareEnvironment() ✨【准备运行环境，加载yml/properties配置文件】
        │        └── 加载 application.yml，绑定所有配置属性
        │             👉 spring.redis、spring.datasource、shardingsphere全部在这里解析
        │             👉 snakeyaml版本冲突TagInspector报错，发生在此阶段
        ├── Step5: 打印Spring Banner ASCII图标
        ├── Step6: createApplicationContext() 创建Spring上下文容器
        │        👉 Web Servlet环境创建 AnnotationConfigServletWebServerApplicationContext
        ├── Step7: prepareContext() 准备上下文
        │        ├── 绑定前面加载完成的Environment环境对象
        │        ├── 执行ApplicationContextInitializer初始化器
        │        ├── 将启动类Boot01Application注册为配置Bean
        │        └── 发布 contextPrepared、contextLoaded 生命周期事件
        ├── Step8: refreshContext(context) ✨【Spring IoC容器核心刷新】
        │        └── refresh() 父类 AbstractApplicationContext，标准Spring容器刷新流程
        │            ├── 1. prepareRefresh 准备刷新
        │            ├── 2. obtainFreshBeanFactory 获取Bean工厂
        │            ├── 3. prepareBeanFactory 预处理BeanFactory
        │            ├── 4. postProcessBeanFactory BeanFactory后置处理
        │            ├── 5. invokeBeanFactoryPostProcessors ✨【重点】
        │            │       └── ConfigurationClassPostProcessor 处理全部@Configuration配置类
        │            │           ├── 解析 @SpringBootApplication复合注解
        │            │           ├── 触发 @EnableAutoConfiguration，执行AutoConfigurationImportSelector
        │            │           ├── 执行@ComponentScan扫描Controller、Service、Repository
        │            │           ├── 读取自动配置清单，执行@Conditional条件过滤
        │            ├── 6. registerBeanPostProcessors 注册Bean后置处理器(AOP核心)
        │            ├── 7. initMessageSource 国际化组件初始化
        │            ├── 8. initApplicationEventMulticaster 事件广播器
        │            ├── 9. onRefresh() ✨ ServletWebServerApplicationContext重写
        │            │        └️ 启动内嵌Tomcat，本项目80端口在这里完成绑定
        │            ├── 10. registerListeners 注册事件监听器
        │            ├── 11. finishBeanFactoryInitialization ✨实例化所有非懒加载单例Bean
        │            │        ├── RedisTemplate、RabbitTemplate、HikariDataSource、Mybatis组件
        │            │        ├── Controller、Service、MQ消费者、ShardingSphere数据源
        │            │        ├── 执行 @PostConstruct、InitializingBean初始化方法
        │            └── 12. finishRefresh 容器刷新完成，发布刷新完成事件
        ├── Step9: afterRefresh 刷新完成后回调
        ├── Step10: 发布started事件，代表Spring容器启动完毕
        └── Step11: 执行 ApplicationRunner / CommandLineRunner 启动后执行器
```

### 和本项目现象对应

1. **prepareEnvironment()**：加载yml配置；snakeyaml版本冲突报错就发生在这里。
2. **invokeBeanFactoryPostProcessors**：解析配置类，执行自动配置选择器。
3. **onRefresh()**：启动内嵌Tomcat，控制台打印`Tomcat started on port 80 (http)`。
4. **finishBeanFactoryInitialization**：实例化所有中间件Bean，Quartz定时任务在此处开始调度。

>
> Redis、RabbitMQ、MyBatis、ShardingSphere全部在`refresh()`容器刷新流程完成实例化。

## 四、@EnableAutoConfiguration 完整数据流转

```
Boot01Application @SpringBootApplication
        ↓
@EnableAutoConfiguration
        ↓
@Import(AutoConfigurationImportSelector.class)
        ↓
AutoConfigurationImportSelector.selectImports()
        ↓
读取 classpath下 META‑INF/spring/org.springframework.boot.autoconfigure.imports
        ↓
得到大量自动配置类全限定名集合
        ↓
逐个解析自动配置类上 @ConditionalOnXXX 条件注解
        ↓
✅条件满足：配置类加入容器；❌条件不满足：直接跳过，不加载
        ↓
满足条件的配置类被解析，内部@Bean方法执行创建对象
        ↓
Bean存入IoC容器，业务代码直接@Autowired注入使用
```

### RedisAutoConfiguration简化源码示例

```
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class) // classpath存在RedisOperations才生效
@EnableConfigurationProperties(RedisProperties.class) // 将yml spring.redis.*绑定到实体类
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // SpringBoot默认RedisTemplate，JDK序列化，存入Redis产生二进制乱码
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
```

>
> 📚项目对应知识点
>
>
> 1. `@EnableConfigurationProperties(RedisProperties.class)`：把yml中`spring.redis.*`配置绑定到RedisProperties实体。
> 2. `@ConditionalOnMissingBean(name = "redisTemplate")`：我们自定义RedisTemplate Bean之后，自动配置不会再创建默认实例。
> 3. 默认RedisTemplate使用JDK序列化，存入Redis出现乱码，就是该类生成的对象。

## 五、@EnableConfigurationProperties 配置绑定原理

作用：将 `application.yml` 的配置属性绑定到Java POJO实体。
例如：`RedisProperties`、`RabbitProperties`、`DataSourceProperties`，注解`@ConfigurationProperties(prefix = "spring.redis")`

流转流程：

1. `@EnableConfigurationProperties(XxxProperties.class)` 将Properties类注册为Bean
2. 底层 `ConfigurationPropertiesBindingPostProcessor` Bean后置处理器
3. 扫描类上`@ConfigurationProperties`注解
4. 从Environment对象读取对应prefix前缀的配置
5. 通过反射调用setter方法完成实体属性赋值

>
> 业务开发中，自定义配置也可以使用该机制读取yml自定义配置。

## 六、项目中三种配置模式对比

1. **SpringBoot自动配置 @EnableAutoConfiguration**
    - Bean来自`spring‑boot‑autoconfigure` jar包；依靠条件注解按需装配；Redis、RabbitMQ、WebMvc、DataSource。
2. **自定义配置类 @Configuration + @Bean**
    - 项目内`RedisConfig.java`、`RabbitMQConfig.java`；用于覆盖、增强默认Bean，例如修改Redis序列化、声明交换机队列。
3. **组件扫描 @ComponentScan**
    - `@Service @Controller @Repository`业务层组件，组件扫描注册进入IoC容器。

>
> 优先级：自定义@Bean > SpringBoot自动配置Bean，依靠`@ConditionalOnMissingBean`实现。

## 七、项目踩坑 & 底层原理对应

### 坑1：自定义RedisTemplate序列化不生效

- 原理：RedisAutoConfiguration中`@ConditionalOnMissingBean(name = "redisTemplate")`
- 解决：自定义@Bean方法名称必须为`redisTemplate`，覆盖自动配置。

### 坑2：snakeyaml TagInspector 类找不到

- 原理：发生在`prepareEnvironment()`解析yml；SpringBoot3.2.5需要snakeyaml2.x；ShardingSphere5.4.1强制依赖snakeyaml1.x，版本冲突。
- 解决：升级ShardingSphere至5.5.1，兼容snakeyaml2.x。

### 坑3：@Autowired 报 NoSuchBeanDefinitionException

- 原理：`@ComponentScan`只会扫描启动类包及其子包；Bean所在包不在扫描范围不会注册。
- 解决：业务代码放在`com.hz.boot01`子包；或者手动指定`@ComponentScan(basePackages = "xxx")`。

### 坑4：修改yml配置，程序读取不到更新的值

- 原理：Maven编译target目录resources没有更新，读取旧配置文件。
- 解决：Maven clean，执行Rebuild Project。

### 坑5：server.port端口配置不生效

- 原理：`ServerProperties`绑定yml端口；手动创建`TomcatServletWebServerFactory` @Bean会覆盖yml配置。

## 八、面试简答（结合本项目记忆）

1. **@SpringBootApplication包含哪几个注解？**`@Configuration`、`@EnableAutoConfiguration`、`@ComponentScan`。
2. **@EnableAutoConfiguration做了什么？**
   导入`AutoConfigurationImportSelector`，读取自动配置清单文件；通过`@ConditionalOnXXX`条件注解过滤配置类，符合条件的Bean注册到IoC容器。
3. **SpringApplication.run()主要步骤？**
   ①prepareEnvironment加载yml配置；②创建IoC上下文；③执行容器refresh()刷新；refresh中完成配置类解析、组件扫描、自动配置装配、启动内嵌Tomcat、实例化全部单例Bean。
4. **如何覆盖SpringBoot默认自动配置Bean？**
   自定义`@Configuration`配置类，手写同名`@Bean`；自动配置类带有`@ConditionalOnMissingBean`，检测容器已存在该Bean，就跳过创建。项目Redis序列化就是典型实践。
5. **SpringBoot2与SpringBoot3自动配置文件区别？**

- SpringBoot2：`META‑INF/spring.factories`
- SpringBoot3：`META‑INF/spring/org.springframework.boot.autoconfigure.imports`

## 九、延伸学习方向

1. 自定义`@Conditional`条件注解，实现条件装配。
2. SpringBoot Starter原理：starter只是聚合pom依赖；真正自动配置代码在`spring‑boot‑autoconfigure`。
3. SpringBoot事件监听`ApplicationListener`，理解各个生命周期事件。
4. Environment抽象：统一封装yml、properties、环境变量、命令行参数。

---