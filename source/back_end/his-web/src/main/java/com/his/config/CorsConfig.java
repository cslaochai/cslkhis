package com.his.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置：允许前端（独立域名/端口）直接调本服务。
 *
 * <p>形态是 Servlet 过滤器（CorsFilter），不是 Spring Security 的 http.cors()。代价是：
 * 容器里 Filter 的默认顺序排在 springSecurityFilterChain（-100）之后，所以鉴权层直接吐出的
 * 401/403 响应不带 CORS 头——真跨域时浏览器只报「CORS 失败」，看不到 401/403 本身。
 * 当前前端走 dev 代理属同源，问题不暴露；一旦独立域名部署，应改为暴露 CorsConfigurationSource
 * 并在 SecurityConfig 里 .cors(Customizer.withDefaults())，否则登录过期时前端拿不到 401，
 * 也就不会清 token 回登录页。
 *
 * <p>允许携带凭证（allowCredentials）时来源不能写字面量 "*"：浏览器会把「* + 凭证」直接拒掉，
 * 必须用 originPattern，框架才会按实际 Origin 回显。
 */
@Configuration
public class CorsConfig {

    /**
     * 全局跨域规则：所有路径、所有方法、所有请求头，允许带 Cookie/Authorization。
     * 收紧时在这里按路径分组 registerCorsConfiguration，不要改到 SecurityConfig 去。
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
