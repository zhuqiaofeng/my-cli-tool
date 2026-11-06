package com.hz.boot01.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================
 * RabbitMQ 核心概念学习笔记
 * ============================================================
 *
 * 【数据流转全流程】
 *
 *   生产者(Producer)          交换机(Exchange)           队列(Queue)          消费者(Consumer)
 *  ┌─────────────┐        ┌──────────────────┐      ┌──────────────────┐      ┌─────────────┐
 *  │ MqController │──────▶│ exchange.direct  │─────▶│  queue.hello     │─────▶│ HelloMq     │
 *  │ (发消息)     │  发送   │ (路由器/分发中心)  │  路由  │  (存消息的仓库)   │  监听 │ Consumer    │
 *  └─────────────┘        └──────────────────┘      └──────────────────┘      └─────────────┘
 *
 *  【关键点】
 *  1. 生产者不直接把消息发给队列！而是发给交换机
 *  2. 交换机根据「路由键(Routing Key)」决定把消息发给哪个队列
 *  3. 队列和交换机之间需要「绑定(Binding)」关系，指定路由键
 *  4. 消费者只监听队列，队列里有消息就自动推给消费者
 *
 *  【为什么要交换机？直接发队列不行吗？】
 *  解耦：生产者只负责发消息到交换机，不需要知道有哪些队列、哪些消费者
 *  灵活：同一个交换机可以绑定多个队列，一条消息可以同时发给多个队列
 * ============================================================
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 名称常量 ====================
    // 队列名称：消息真正存放的地方，消费者从这里取消息
    public static final String QUEUE_HELLO = "queue.hello";
    public static final String QUEUE_EMAIL = "queue.email";

    // 交换机名称：消息的分发中心，所有消息先到这里，再由它转发给队列
    public static final String EXCHANGE_DIRECT = "exchange.direct";
    public static final String EXCHANGE_TOPIC = "exchange.topic";

    // 路由键：交换机根据这个 key 决定把消息发给哪个队列
    // 就像快递的「详细地址」，交换机按地址把包裹送到对应的快递柜(队列)
    public static final String ROUTING_HELLO = "routing.hello";
    public static final String ROUTING_EMAIL = "routing.email";

    // ==================== 声明队列 ====================

    /**
     * 声明 hello 队列
     *
     * 【方法调用时机】
     * 项目启动时，Spring 会自动执行这个方法，在 RabbitMQ 服务器上创建这个队列
     * 如果队列已经存在，就不会重复创建（RabbitMQ 保证幂等）
     *
     * @param durable=true  持久化：RabbitMQ 重启后队列还在，消息不丢
     */
    @Bean
    public Queue helloQueue() {
        return new Queue(QUEUE_HELLO, true);
    }

    /**
     * 声明 email 队列（专门用来存邮件消息）
     */
    @Bean
    public Queue emailQueue() {
        return new Queue(QUEUE_EMAIL, true);
    }

    // ==================== 声明交换机 ====================

    /**
     * 声明 Direct 交换机（直连交换机）
     *
     * 【Direct 交换机的特点】
     * 路由键必须「完全匹配」才会转发
     * 比如路由键是 "routing.hello"，就只会发给绑定了 "routing.hello" 的队列
     * 就像打电话，必须拨对完整号码才能接通
     *
     * @param durable=true  持久化
     * @param autoDelete=false  没有绑定队列时自动删除（这里设为不自动删）
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    // ==================== 绑定关系 ====================

    /**
     * 把 hello 队列绑定到 direct 交换机，指定路由键 routing.hello
     *
     * 【这行代码做了什么？】
     * 告诉 RabbitMQ：凡是发到 exchange.direct、路由键是 routing.hello 的消息，
     * 都转发给 queue.hello 队列
     *
     * 【类比】
     * 就像给快递柜设规则：凡是收件地址写 "routing.hello" 的包裹，
     * 都放进 1 号快递柜(queue.hello)
     */
    @Bean
    public Binding bindingHello(Queue helloQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(helloQueue).to(directExchange).with(ROUTING_HELLO);
    }

    /**
     * 把 email 队列绑定到 direct 交换机，指定路由键 routing.email
     * 路由键是 routing.email 的消息 → 发给 queue.email 队列
     */
    @Bean
    public Binding bindingEmail(Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with(ROUTING_EMAIL);
    }
}
