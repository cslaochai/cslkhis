package com.his.common.config;

import com.fasterxml.jackson.databind.Module;
import com.his.common.jackson.TrimModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 公共 Jackson 配置：注册 {@link TrimModule}（全局 String 字段自动 trim + 黑名单排除）。
 *
 * <p>Spring Boot 自动收集容器内所有 Module Bean 并注册到全局 ObjectMapper，
 * 故全应用所有 @RequestBody 的 String 字段都会自动去空格（敏感字段除外）。</p>
 */
@Configuration
public class CommonJacksonConfig {

    @Bean
    public Module trimModule() {
        return new TrimModule();
    }
}
