package com.hz.boot01.service;

import com.hz.boot01.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 邮件消息消费者
 *
 * 【这个消费者的作用】
 * 专门处理 queue.email 队列里的消息
 * 实际业务里，收到消息后就去调用发邮件的接口
 *
 * 【和 HelloMqConsumer 的区别】
 * 就是监听不同的队列，处理不同的业务
 * 一个队列可以有多个消费者（提高处理速度）
 * 一个消费者也可以监听多个队列（看你怎么配）
 */
@Component
public class EmailMqConsumer {

    /**
     * 监听 queue.email 队列
     *
     * 【完整调用链】
     * 1. MqController.sendEmail() 发消息到 exchange.direct
     * 2. 交换机根据 routing.email 路由键，把消息发给 queue.email
     * 3. 这个监听器收到消息，自动执行这个方法
     * 4. 这里写真正发邮件的代码（学习阶段先打印）
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void listenEmailQueue(String message) {
        System.out.println("【消费者2】收到邮件队列消息：" + message);
        // TODO 实际业务：调用 JavaMailSender 真正发送邮件
    }
}
