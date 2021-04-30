package com.dlq.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2021-03-01 13:20
 *
 * 定时任务
 *      1、@EnableScheduling 开启定时任务
 *      2、@Scheduled 开启一个定时任务
 *      3、 自动配置类 TaskSchedulingAutoConfiguration
 *
 * 异步任务
 *      1、@EnableAsync 开启异步任务功能
 *      2、@Async 给希望异步执行的方法上标注@Async
 *      3、自动配置类 TaskExecutionAutoConfiguration 属性绑定在 spring.task.execution
 */
@Slf4j
@Component
//@EnableAsync
//@EnableScheduling
public class HelloSchedule {

    /**
     * 1、spring中6位组成，不允许第7位的年
     * 2、在周几的位置，1-7代表周一到周日；MON-SUN
     * 3、定时任务不应该阻塞。默认是阻塞的
     *      1)、可以让业务运行以异步的方式，自己提交到线程池
     *          CompletableFuture.runAsync(()->{
     *              xxxService.hello();
     *          },executr);
     *      2)、支持定时任务线程池；设置 TaskSchedulingProperties
     *              spring.task.scheduling.pool.size=5  --> springboot2.2.2 不好使
     *
     *      3)、让定时任务异步执行
     *           异步任务
     *
     *    解决：使用异步+定时任务来完成定时任务不阻塞的功能
     */
    @Async
    @Scheduled(cron = "* * * ? * 1")
    public void hello(){
        log.info("hello...");
        //try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}