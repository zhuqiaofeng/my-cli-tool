package com.hz.boot01.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 *
 * 【设计思路】
 * Spring Boot 自动配置的 RedisTemplate 默认使用 JDK 序列化方式，
 * 导致存入 Redis 的 key 和 value 都是乱码，不方便查看和调试。
 *
 * 我们需要自定义 RedisTemplate，指定：
 * 1. key 用 StringRedisSerializer 序列化（字符串可读）
 * 2. value 用 Jackson2JsonRedisSerializer 序列化（JSON 格式，可读且跨语言）
 *
 * 【学习要点】
 * - 为什么要自定义 RedisTemplate？
 *   因为默认的 JDK 序列化：存储体积大、可读性差、无法跨语言
 * - StringRedisSerializer vs Jackson2JsonRedisSerializer？
 *   StringRedisSerializer：适合字符串 key，人类可读
 *   Jackson2JsonRedisSerializer：适合对象 value，转 JSON 格式
 * - RedisConnectionFactory 是什么？
 *   Spring Boot 自动配置的 Redis 连接工厂，负责管理 Redis 连接池
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义 RedisTemplate Bean
     *
     * @param redisConnectionFactory Spring Boot 自动注入的 Redis 连接工厂
     * @return 配置好序列化规则的 RedisTemplate
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        // 创建 RedisTemplate 实例，指定 key 和 value 的泛型
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // ========== 1. 设置 key 的序列化方式 ==========
        // key 统一用 String 序列化，这样在 Redis 客户端里能直接看到 key 名称
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // ========== 2. 设置 value 的序列化方式 ==========
        // value 用 Jackson JSON 序列化，把对象转成 JSON 字符串存储
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        // 配置 ObjectMapper，解决 Jackson 序列化时的一些问题
        ObjectMapper objectMapper = new ObjectMapper();
        // 指定要序列化的域，field,get 和 set，以及修饰符范围，ANY 是都有包括 private 和 public
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非 final 修饰的，比如 String 是 final 的，会报异常
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 初始化设置
        template.afterPropertiesSet();

        return template;
    }
}
