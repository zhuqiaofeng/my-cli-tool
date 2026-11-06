package com.hz.boot01.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 具体的定时任务类
 *
 * 【注意】
 * 必须实现 Job 接口，重写 execute 方法
 * 这个方法里写你要定时执行的代码
 *
 * 【执行时机】
 * 到了 Trigger 设定的时间，Quartz 就会自动调用这个方法
 * 不用你自己写循环
 */
public class HelloJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 这里写具体的任务逻辑
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("【定时任务】现在时间：" + now + "，Hello Quartz！");
    }
}
