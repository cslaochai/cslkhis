package com.his.clinical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 门诊结诊收尾（自动质控 + 待缴账单）专用线程池。
 *
 * <p><b>为什么要自己建一个</b>：容器默认的执行器队列是无界的，收尾一旦比结诊慢就一直堆积，
 * 进程重启时整批静默丢掉、库里连一条痕迹都没有。有界队列把「积压」变成看得见的拒绝，
 * 被拒的病历由定时补扫按「结诊了却没有自动质控单」捞回来。
 *
 * <p>拒绝策略用 Abort 而不是 CallerRuns：收尾不能回落到请求线程里跑，
 * 否则医生点结诊又会被后台质控拖住（这正是当初把它挪成异步的原因）。
 */
@Configuration
public class ClinicAfterSubmitExecutorConfig {

    public static final String BEAN_NAME = "clinicAfterSubmitExecutor";

    @Bean(BEAN_NAME)
    public ThreadPoolTaskExecutor clinicAfterSubmitExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("clinic-after-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}
