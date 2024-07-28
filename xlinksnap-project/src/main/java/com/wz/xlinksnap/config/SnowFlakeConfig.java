package com.wz.xlinksnap.config;

import com.wz.xlinksnap.common.generation.SnowFlake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 雪花算法配置类
 */
@Configuration
public class SnowFlakeConfig {

    @Bean
    public SnowFlake snowFlake() {
        // 初始化datacenterId和machineId，例如：1和2
        return new SnowFlake(1, 2);
    }
}
