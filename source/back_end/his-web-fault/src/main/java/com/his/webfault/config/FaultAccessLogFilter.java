package com.his.webfault.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 只把「触发动作」写进应用日志，GET /fault/status 的 5 秒轮询不刷屏。
 * 日志同时输出控制台和 run/logs/his-web-fault.log，虚拟机采集这个文件即可看到每次触发。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FaultAccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FaultAccessLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod()) || !request.getRequestURI().startsWith("/fault/")) {
            filterChain.doFilter(request, response);
            return;
        }
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("fault-trigger uri={} query={} status={} cost={}ms",
                    request.getRequestURI(),
                    request.getQueryString(),
                    response.getStatus(),
                    System.currentTimeMillis() - start);
        }
    }
}
