package com.wz.xlinksnap.common.properties;

import com.wz.xlinksnap.common.constant.ThreadPoolConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池配置项
 */
//Spring 会自动绑定属性文件中定义的、前缀为 设置的前缀 且名称与 该类中某个字段相同的任何属性。
@ConfigurationProperties(prefix = ThreadPoolConstant.PREFIX)
@Data
public class ConcurrentProperties {

    ThreadPool threadPool = new ThreadPool();

    /**
     * 线程池配置
     */
    @Data
    public static class ThreadPool {

        /**
         * 线程池初始线程数
         */
        int coreSize = numProcessors();
        /**
         * 线程池最大线程数
         */
        int maxSize = numProcessors() * 2;
        /**
         * 线程池的等待队列长度限制
         */
        int queueSize = 100;
        /**
         * 是否采用丢弃任务的拒绝策略:AbortPolicy。默认是false，会采用CallerRunsPolicy防止任务丢失
         */
        boolean abortPolicy = false;
        /**
         * 异步处理线程池的名字前缀
         */
        String prefix = "xlink-thread-pool";

        /**
         * 默认的coreSize与maxSize是根据当前可用核数动态计算的
         * 且maxSize = coreSize * 2
         * 如果需要改变默认值，coreSize与maxSize需同时指定新的值
         * @return 默认初始线程数
         */
        private static int numProcessors() {
            int n = Runtime.getRuntime().availableProcessors();
            return Math.max(n, 10);
        }
    }


}