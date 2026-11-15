package com.his.webfault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 允许前端页面跨域调用演练接口。
 *
 * <p>演练进程只绑 127.0.0.1（见 application.yml），前端 dev server 在 3000 端口、
 * 构建产物也可能由 his-web 的 8080 端口托管，浏览器侧属于不同 Origin，不加 CORS
 * 会在预检阶段被拦。这里只放行 /fault/**，actuator 不开放跨域。</p>
 *
 * <p>不携带 Cookie（allowCredentials 保持 false），因此 allowedOriginPatterns 用通配
 * 是安全的：无凭据的跨域请求不会把用户会话带进来。真正的边界仍是「只绑回环」。</p>
 */
@Configuration
public class FaultLabCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/fault/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
