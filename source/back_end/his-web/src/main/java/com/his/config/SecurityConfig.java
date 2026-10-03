package com.his.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.base.Result;
import com.his.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] WHITE_LIST = {
            // 登录注册
            "/auth/login",
            "/auth/register",
            // 小程序端患者自助注册（建档并开通账号，匿名可访问，否则新患者无法注册）
            "/patient/register",
            // 注册验证码下发（同上，注册前无 token）
            "/patient/sms/sendCode",
            // 患者端微信一键登录（小程序一期口子；openid 换取后才可能有问题，code 本身匿名可带）
            "/miniapp/auth/wxLogin",

            // Swagger API 文档
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v2/api-docs/**",
            "/webjars/**",

            // 静态资源
            "/favicon.ico",
            "/static/**",
            "/public/**",
            "/resources/**",

            // 健康检查
            "/actuator/**",
            "/health",
            "/ping",
            "/uploads/**",

            // 错误页面
            "/error"
    };
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                // 未认证 / 无权限也必须走统一 Result 报文。
                // 不配这两个 handler 时 Spring Security 默认返回 <b>HTTP 403 且响应体为空</b>：
                // 一是语义错（没登录应该是 401），二是前端拿不到 message，只能看到「未知错误」。
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJson(response, HttpStatus.UNAUTHORIZED, Result.error(401, "未登录或登录已过期")))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJson(response, HttpStatus.FORBIDDEN, Result.error(403, "没有权限访问")))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable()) // 全局关闭
                );
        return http.build();
    }

    /**
     * 以统一 Result 结构写回错误响应（HTTP 状态码与 body.code 保持一致，前端拦截器按此判断）。
     */
    private void writeJson(HttpServletResponse response, HttpStatus status, Result<?> body) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
