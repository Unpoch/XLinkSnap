package com.wz.xlinksnap.config;

import com.alibaba.ttl.TtlRunnable;
import com.wz.xlinksnap.common.concurrent.ConcurrentJobExecutor;
import com.wz.xlinksnap.common.properties.ConcurrentProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ConcurrentProperties.class)
public class ConcurrentConfig {

    @Autowired
    ConcurrentProperties concurrentProperties;

    @Bean
    ConcurrentJobExecutor conCurrentExecutor() {
        val threadPoolProps = concurrentProperties.getThreadPool();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();//Spring提供的线程池：基于ThreadPoolExecutor线程池
        int coreSize = threadPoolProps.getCoreSize();
        executor.setCorePoolSize(coreSize);
        int maxSize = threadPoolProps.getMaxSize();
        executor.setMaxPoolSize(maxSize);
        int queueSize = threadPoolProps.getQueueSize();
        executor.setQueueCapacity(queueSize);
        String prefix = threadPoolProps.getPrefix();
        executor.setThreadNamePrefix(prefix);
        executor.setTaskDecorator(TtlRunnable::get);//装饰器，异步多线程中传递上下文等变量
        boolean abortPolicy = threadPoolProps.isAbortPolicy();
        if (abortPolicy) {
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        } else {
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }
        executor.afterPropertiesSet();
        log.info("配置线程池: core={} max={} queue={} prefix={}",
                coreSize, maxSize, queueSize, prefix);
        return new ConcurrentJobExecutor(executor);
    }

}