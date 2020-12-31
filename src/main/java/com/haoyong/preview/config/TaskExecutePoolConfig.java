package com.haoyong.preview.config;

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
public class TaskExecutePoolConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor poolExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数
        poolExecutor.setCorePoolSize(5);
        // 最大线程数
        poolExecutor.setMaxPoolSize(15);
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
