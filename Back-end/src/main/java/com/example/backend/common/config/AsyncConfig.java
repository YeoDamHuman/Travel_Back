package com.example.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); //기본 쓰레드 2개
        executor.setMaxPoolSize(10); //최대 쓰레드 10개
        executor.setQueueCapacity(500); //대기열 500개
        executor.setThreadNamePrefix("Async-"); //쓰레드 이름
        executor.initialize();
        return executor;
    }
}