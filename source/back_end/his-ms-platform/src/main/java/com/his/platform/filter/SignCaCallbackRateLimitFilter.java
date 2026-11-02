package com.his.platform.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.base.Result;
import com.his.common.util.TextUtil;
import com.his.platform.config.SignProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CA 推送回调限流：同一来源 IP 每分钟最多 {@code his.sign.ca.callback-rate-limit-per-minute} 次，超出直接 429。
 *
 * <p>CA 推送端点匿名开放（CA 服务器不持有 HIS token），限流是防刷的第一道边界；
 * 计数放在内存里按「IP + 当前分钟」分桶，无需引入 Redis——单实例本系统足够，
 * 多实例部署时各实例独立计数（总量会放大，但仍能压住单实例被刷爆）。</p>
 *
 * <p><b>必须排在验签之前</b>（{@code HIGHEST_PRECEDENCE + 10}，验签为 {@code + 20}）：
 * 验签失败会直接 {@code return}、不往下走链，若限流排在其后，伪造签名的请求既不计数也永不触发 429，
 * 攻击者可用无效报文无限次压 SHA256 + HMAC（还要先 {@code readAllBytes()} 读满请求体），
 * 限流反而只卡住了正常流量。先计数再验签，失败的请求同样计入分钟桶。</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class SignCaCallbackRateLimitFilter extends OncePerRequestFilter {

    /**
     * 限流作用路径（已去掉 context-path 的内部路径，与 SecurityConfig 白名单一致）
     */
    private static final String CALLBACK_PATH = "/emr/signCert/caCallback";

    /**
     * 内存分桶的软上限：超过后清理过期分钟桶，避免长期运行时 key 无限增长
     */
    private static final int PURGE_THRESHOLD = 4096;

    private final SignProperties signProperties;

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !CALLBACK_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        int limit = signProperties.getCa().getCallbackRateLimitPerMinute();
        if (limit <= 0) {
            // 配 0 或负数表示不限流（仅建议在受信内网且已有网关限流时使用）
            filterChain.doFilter(request, response);
            return;
        }
        long currentMinute = System.currentTimeMillis() / 60_000L;
        String ip = clientIp(request);
        String key = ip + "#" + currentMinute;
        int count = counters.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        if (counters.size() > PURGE_THRESHOLD) {
            purgeExpired(currentMinute);
        }
        if (count > limit) {
            log.warn("CA 推送回调超过限流阈值 ip={} 本分钟第{}次 阈值={}", ip, count, limit);
            writeTooManyRequests(response, limit);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 取来源 IP。
     *
     * <p>默认只认 {@code getRemoteAddr()}：本端点直连对外端口，客户端可以直接伪造
     * {@code X-Forwarded-For} / {@code X-Real-IP}，按头取 IP 等于把限流键交给攻击者，
     * 每次换个伪造值就能绕过分钟桶。只有 {@code his.sign.ca.callback-trust-forwarded-ip=true}
     * （确认部署在强制覆盖该头的受信代理之后）才读头。</p>
     *
     * <p>读头时优先 {@code X-Real-IP}，其次取 {@code X-Forwarded-For} 的<b>最后一段</b>：
     * nginx 的 {@code $proxy_add_x_forwarded_for} 把真实地址追加在末尾，
     * 首段是客户端可自行塞入的内容，取首段正是常见的伪造入口。</p>
     */
    private String clientIp(HttpServletRequest request) {
        if (!signProperties.getCa().isCallbackTrustForwardedIp()) {
            return request.getRemoteAddr();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (TextUtil.hasText(realIp)) {
            return realIp.trim();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (TextUtil.hasText(forwarded)) {
            String[] hops = forwarded.split(",");
            String last = hops[hops.length - 1].trim();
            if (TextUtil.hasText(last)) {
                return last;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 清理非当前分钟的计数桶：只保留正在计数的这一分钟，下一分钟从零开始。
     */
    private void purgeExpired(long currentMinute) {
        String suffix = "#" + currentMinute;
        Iterator<Map.Entry<String, AtomicInteger>> it = counters.entrySet().iterator();
        while (it.hasNext()) {
            if (!it.next().getKey().endsWith(suffix)) {
                it.remove();
            }
        }
    }

    /**
     * 请求过多状态码（429）：Servlet 常量表里没有 429，这里显式声明
     */
    private static final int TOO_MANY_REQUESTS = 429;

    private void writeTooManyRequests(HttpServletResponse response, int limit) throws IOException {
        response.setStatus(TOO_MANY_REQUESTS);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(TOO_MANY_REQUESTS,
                        "CA 推送请求过于频繁（每分钟上限 " + limit + " 次），本次已拒绝")));
    }
}
