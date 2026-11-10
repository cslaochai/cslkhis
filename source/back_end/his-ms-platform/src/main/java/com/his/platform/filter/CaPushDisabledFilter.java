package com.his.platform.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.base.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CA 推送端点关停开关：{@code his.sign.ca.push-verify-enabled=false} 时生效。
 *
 * <p>「停用验签」绝不能被理解成「放行不校验」——那是最危险的降级。
 * 本 Filter 把端点直接回 503，保证无论是否验签，匿名写入口在停用状态下都不接受任何报文。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "his.sign.ca", name = "push-verify-enabled", havingValue = "false")
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CaPushDisabledFilter extends OncePerRequestFilter {

    private static final String CALLBACK_PATH = "/emr/signCert/caCallback";

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !CALLBACK_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.warn("CA 推送回调已被停用（his.sign.ca.push-verify-enabled=false），拒绝来源 ip={}",
                request.getRemoteAddr());
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.error(503, "CA 推送回调已停用，不接收推送")));
    }
}
