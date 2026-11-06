package com.hz.boot01.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================
 * XXL-Job 执行器配置
 * ============================================================
 *
 * 【XXL-Job 架构】
 *
 *   ┌─────────────────┐         ┌─────────────────┐
 *   │  调度中心        │────通信──▶│  执行器(你的项目) │
 *   │  xxl-job-admin  │         │  负责真正执行任务  │
 *   └─────────────────┘         └─────────────────┘
 *
 *  调度中心：统一管理所有任务，什么时候执行、执行哪个
 *  执行器：就是你的 Spring Boot 项目，收到调度中心的命令后，真正执行任务
 *
 *  【和 Quartz 的区别】
 *  Quartz：任务时间写死在代码里，改时间要改代码重启
 *  XXL-Job：任务时间在网页后台配置，点鼠标就能改，不用改代码
 * ============================================================
 */
// @Configuration  // 先注释掉，等装了调度中心再打开
public class XxlJobConfig {

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        // 调度中心地址（等你装了调度中心，就是这个地址）
        executor.setAdminAddresses("http://127.0.0.1:8080/xxl-job-admin");
        // 执行器名称，调度中心里要选这个
        executor.setAppname("boot01-executor");
        // 执行器端口，9999 不会和你的项目 8080 冲突
        executor.setPort(9999);
        return executor;
    }
}
