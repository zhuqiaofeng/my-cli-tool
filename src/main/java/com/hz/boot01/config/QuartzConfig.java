package com.hz.boot01.config;

import com.hz.boot01.job.HelloJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================
 * Quartz 定时任务核心概念学习笔记
 * ============================================================
 *
 * 【四个核心角色】
 *
 *   ┌─────────┐      ┌──────────┐      ┌──────────┐      ┌─────────┐
 *   │ JobDetail │────▶ │ Trigger   │────▶ │ Scheduler │────▶ │  Job    │
 *   │ (要做什么) │      │ (什么时候做)│      │ (调度中心) │      │ (具体任务)│
 *   └─────────┘      └──────────┘      └──────────┘      └─────────┘
 *
 *  1. Job：具体要执行的任务逻辑（比如：打印日志、备份数据库）
 *  2. JobDetail：定义任务的详情，比如任务名字、属于哪个组
 *  3. Trigger：触发器，定义任务什么时候执行（比如：每秒一次、每天凌晨2点）
 *  4. Scheduler：调度中心，把 JobDetail 和 Trigger 绑定在一起，到点自动执行
 *
 *  【类比】
 *  Job = 你要做的事（比如：写作业）
 *  JobDetail = 这件事的名片（名字叫"写作业"）
 *  Trigger = 闹钟设定（每天晚上8点开始写）
 *  Scheduler = 你自己，到点了就去写作业
 * ============================================================
 */
@Configuration
public class QuartzConfig {

    /**
     * 定义 JobDetail：告诉 Quartz 要执行哪个任务
     */
    @Bean
    public JobDetail helloJobDetail() {
        return JobBuilder.newJob(HelloJob.class)  // 要执行哪个 Job 类
                .withIdentity("helloJob")          // 任务的唯一标识
                .storeDurably()                    // 任务持久化，没有 Trigger 也保留
                .build();
    }

    /**
     * 定义 Trigger：告诉 Quartz 这个任务什么时候执行
     *
     * 【简单 Trigger】：固定间隔执行，比如每 5 秒一次
     * 【Cron Trigger】：用 cron 表达式，更灵活，比如每天凌晨 2 点
     */
    @Bean
    public Trigger helloJobTrigger(JobDetail helloJobDetail) {
        // 用 SimpleScheduleBuilder：每 5 秒执行一次，无限重复
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(5)  // 间隔 5 秒
                .withRepeatCount(0);
                //.repeatForever();           // 一直重复执行

        return TriggerBuilder.newTrigger()
                .forJob(helloJobDetail)        // 绑定哪个 JobDetail
                .withIdentity("helloTrigger")  // 触发器的唯一标识
                .withSchedule(scheduleBuilder) // 用什么调度规则
                .build();
    }
}
