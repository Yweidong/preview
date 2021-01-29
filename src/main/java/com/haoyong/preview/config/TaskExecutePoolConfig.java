package com.haoyong.preview.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: preview
 * @description: 线程池配置
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-12-30 11:41
 **/
@EnableAsync
@Configuration
@Slf4j
public class TaskExecutePoolConfig {

    private static final int THREAD_LINE_NUM = Runtime.getRuntime().availableProcessors();

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor poolExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数
        poolExecutor.setCorePoolSize(THREAD_LINE_NUM);
        // 最大线程数
        poolExecutor.setMaxPoolSize(2*THREAD_LINE_NUM);
        // 队列大小
        poolExecutor.setQueueCapacity(100);
        // 线程最大空闲时间
        poolExecutor.setKeepAliveSeconds(300);
        // 拒绝策略
        poolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程名称前缀
        poolExecutor.setThreadNamePrefix("my-pool-");

        return poolExecutor;
    }
}
