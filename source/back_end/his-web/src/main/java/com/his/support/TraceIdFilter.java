package com.his.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 请求串联：给每个请求一个 traceId，写进 MDC（日志 pattern 用 %X{traceId} 取），并回写响应头 X-Trace-Id。
 *
 * <p>没有它就只有一个下场：一次挂号失败打出十几行日志（含 SQL、下游调用、异常堆栈），
 * 分不清哪几行属于同一次请求，报错只能按时间猜。</p>
 *
 * <p>顺序放在最外层（HIGHEST_PRECEDENCE）：后面的鉴权、业务、SQL 日志都要能带上这个值。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * 响应头名：前端报错截图 / 客服工单带上它，就能直接反查日志
     */
    public static final String HEADER = "X-Trace-Id";

    private static final String MDC_KEY = "traceId";

    /**
     * 只收 8~64 位十六进制或字母数字：外部传进来的头会进日志行，
     * 放宽等于允许「带换行的头」伪造整行日志（日志注入）。
     */
    private static final Pattern VALID = Pattern.compile("[0-9A-Za-z-]{8,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String incoming = request.getHeader(HEADER);
        String traceId = (incoming != null && VALID.matcher(incoming).matches()) ? incoming : newTraceId();
        MDC.put(MDC_KEY, traceId);
        response.setHeader(HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
