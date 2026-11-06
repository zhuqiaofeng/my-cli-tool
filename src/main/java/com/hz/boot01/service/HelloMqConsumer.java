package com.hz.boot01.service;

import com.hz.boot01.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * RabbitMQ 消息消费者（Consumer）
 * ============================================================
 *
 * 【什么是消费者？】
 * 就是收消息、处理消息的人/程序
 * 这里会一直监听队列，队列里一有新消息，就自动调用这个方法处理
 *
 * 【@RabbitListener 注解的作用】
 * 告诉 Spring：这个方法要监听哪个队列
 * 项目启动时，Spring 会自动和 RabbitMQ 建立连接，开始监听
 * 一旦队列里有消息，就自动调用这个方法
 * ============================================================
 */
@Component
public class HelloMqConsumer {

    /**
     * 监听 queue.hello 队列
     *
     * 【执行时机】
     * 只要 queue.hello 队列里来了新消息，这个方法就会自动被调用
     * 你不用自己写循环去拉取消息，Spring 帮你做了
     *
     * 【参数 message】
     * 就是生产者发过来的消息内容（这里是 String 类型）
     *
     * 【完整调用链回顾】
     * 1. MqController.sendHello() 发消息到 queue.hello
     * 2. 消息存在 queue.hello 队列里
     * 3. RabbitMQ 发现队列有新消息，立刻推给这个消费者
     * 4. 自动执行这个方法，打印消息内容
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_HELLO)
    public void listenHelloQueue(String message) {
        System.out.println("【消费者1】收到 hello 队列消息：" + message);
        // 实际业务里，这里可以写：存数据库、调接口、发通知等等
    }
}
