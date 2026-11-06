package com.hz.boot01.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * XXL-Job 示例任务
 *
 * 【@XxlJob 注解】
 * 注解里的名字就是任务的处理器名字
 * 你在调度中心网页里新建任务的时候，选这个名字就行
 *
 * 【和 Quartz 的区别】
 * Quartz：要写 JobDetail、Trigger，配置一堆
 * XXL-Job：只要加个 @XxlJob 注解写个名字就行，任务时间在网页后台配
 */
@Component
public class SampleXxlJob {

    /**
     * 示例任务：打印当前时间
     *
     * 【执行流程】
     * 1. 调度中心到点了，给执行器发命令：执行 "sampleJobHandler"
     * 2. 执行器收到命令，自动调用这个方法
     * 3. 方法执行完，把结果返回给调度中心
     */
    @XxlJob("sampleJobHandler")
    public void sampleJobHandler() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("【XXL-Job】现在时间：" + now + "，Hello XXL-Job！");
    }
}
