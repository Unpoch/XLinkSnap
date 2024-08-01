package com.wz.xlinksnap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.wz.xlinksnap.mapper")
@EnableScheduling
public class XLinkSnapProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(XLinkSnapProjectApplication.class, args);
    }
}
