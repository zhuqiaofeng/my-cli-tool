package com.hz.boot01.controller;

import com.hz.boot01.config.RabbitMQConfig;
import com.hz.boot01.utils.JsonMassage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================
 * RabbitMQ 消息生产者（Producer）
 * ============================================================
 *
 * 【什么是生产者？】
 * 就是发消息的人/程序。这里就是你的 Spring Boot 接口
 *
 * 【RabbitTemplate 是什么？】
 * Spring Boot 封装好的工具类，帮你把消息发给 RabbitMQ 服务器
 * 不用你自己写网络连接、序列化这些底层代码
 * ============================================================
 */
@RestController
@RequestMapping("/mq")
public class MqController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 【发送方式 1：直接发队列（用默认交换机）】
     *
     * 调用方式：GET http://localhost:8080/mq/sendHello?message=你好
     *
     * 【执行流程】
     * 1. 你浏览器访问这个接口，带上 message 参数
     * 2. 调用 rabbitTemplate.convertAndSend(队列名, 消息内容)
     * 3. Spring 自动把消息发给「默认交换机」(名字叫空字符串 "")
     * 4. 默认交换机根据队列名当路由键，直接把消息发给指定队列
     *
     * 【注意】
     * 这种方式最简单，但只能发到指定队列，不能灵活路由
     * 生产环境一般用下面的方式（指定交换机 + 路由键）
     */
    @GetMapping("/sendHello")
    public JsonMassage<String> sendHello(@RequestParam String message) {
        // 参数 1：队列名称
        // 参数 2：消息内容
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_HELLO, message);
        return new JsonMassage<>(200, "发送成功", 0, message);
    }

    /**
     * 【发送方式 2：发交换机 + 路由键（推荐方式）】
     *
     * 调用方式：GET http://localhost:8080/mq/sendEmail?email=test@qq.com
     *
     * 【执行流程】
     * 1. 你浏览器访问这个接口
     * 2. 调用 rabbitTemplate.convertAndSend(交换机名, 路由键, 消息内容)
     * 3. 消息发到 exchange.direct 交换机
     * 4. 交换机看路由键是 routing.email，找到绑定了这个 key 的队列
     * 5. 把消息转发给 queue.email 队列
     * 6. 队列里的 EmailMqConsumer 监听到消息，自动消费
     *
     * 【完整链路】
     * MqController → exchange.direct → routing.email → queue.email → EmailMqConsumer
     */
    @GetMapping("/sendEmail")
    public JsonMassage<String> sendEmail(@RequestParam String email) {
        // 参数 1：交换机名称（不填就用默认交换机）
        // 参数 2：路由键（交换机靠这个判断发给哪个队列）
        // 参数 3：消息内容
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_EMAIL,
                "发送邮件给：" + email
        );
        return new JsonMassage<>(200, "邮件消息已发送", 0, email);
    }
}
