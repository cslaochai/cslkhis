package com.mis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 医保结算经办（医保局端）模拟平台启动类。
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.mis.config")
@MapperScan("com.mis.**.mapper")
public class MisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MisApplication.class, args);
    }
}
