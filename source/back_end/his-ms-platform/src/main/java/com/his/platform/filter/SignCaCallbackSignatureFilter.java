package com.his.platform.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.base.Result;
import com.his.common.util.TextUtil;
import com.his.platform.config.SignProperties;
import com.his.platform.utils.PushSignUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CA 推送回调验签：校验 {@code X-Ca-Timestamp} / {@code X-Ca-Nonce} / {@code X-Ca-Signature}。
 *
 * <p>为什么必须有这一层：回调端点对 CA 匿名开放（CA 服务器不持有 HIS token），
 * 只靠来源 IP 与限流挡不住伪造。任何人都能 POST 一条 {@code status=ISSUED} 且自带
 * 任意 {@code certPem} 的报文，把一张来源不明的证书塞进 HIS 当成某位医生的签名证书。
 * 公钥本身不保密，证书的价值全在"这把公钥绑定到这个人"，这条链路不验签等于把绑定关系交给攻击者。</p>
 *
 * <p>签名串与 CA 侧一致：{@code timestamp + "\n" + nonce + "\n" + sha256Hex(原始请求体字节)}，
 * 用同一 secret 做 HMAC-SHA256。密钥未配置时<b>拒绝一切推送</b>——宁停摆，也不收来源不明的证书。</p>
 *
 * <p><b>必须排在限流之后</b>（{@code HIGHEST_PRECEDENCE + 20}，限流为 {@code + 10}）：本过滤器一旦
 * 验签失败即 {@code return}，不会把请求交给后续过滤器。若排在限流前面，伪造请求就被提前拦下、
 * 不进分钟桶计数，限流形同只对正常流量生效，且无效报文能被无限次用来消耗 SHA256/HMAC 与请求体内存。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class SignCaCallbackSignatureFilter extends OncePerRequestFilter {

    /**
     * 作用路径（已去掉 context-path 的内部路径，与 SecurityConfig 白名单一致）
     */
    private static final String CALLBACK_PATH = "/emr/signCert/caCallback";

    private static final String HEADER_TIMESTAMP = "X-Ca-Timestamp";

    private static final String HEADER_NONCE = "X-Ca-Nonce";

    private static final String HEADER_SIGNATURE = "X-Ca-Signature";

    /**
     * 内存 nonce 表软上限：超过后清理过期项，避免长期运行时 key 无限增长
     */
    private static final int PURGE_THRESHOLD = 4096;

    private final SignProperties signProperties;

    private final ObjectMapper objectMapper;

    /**
     * 已见 nonce → 过期时刻（毫秒）。单实例内存足够：重放窗口只有分钟级，
     * 多实例部署时各实例独立去重，仍能挡住针对单实例的重放。
     */
    private final ConcurrentHashMap<String, Long> nonces = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !CALLBACK_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        SignProperties.Ca ca = signProperties.getCa();
        String secret = ca.getCallbackSecret();
        if (!TextUtil.hasText(secret)) {
            log.error("CA 推送回调未配置 his.sign.ca.callback-secret，已拒绝推送；"
                    + "该端点匿名开放，无验签等于允许任意伪造签发结果");
            writeError(response, 503, "CA 推送回调未配置验签密钥，服务端拒绝接收");
            return;
        }

        String tsRaw = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String signature = request.getHeader(HEADER_SIGNATURE);
        if (!TextUtil.hasText(tsRaw) || !TextUtil.hasText(nonce) || !TextUtil.hasText(signature)) {
            log.warn("CA 推送回调缺少签名头 ip={} uri={}", request.getRemoteAddr(), request.getServletPath());
            writeError(response, 401, "CA 推送缺少签名头");
            return;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(tsRaw.trim());
        } catch (NumberFormatException e) {
            writeError(response, 401, "CA 推送时间戳格式无效");
            return;
        }
        long skewMs = ca.getCallbackClockSkewSeconds() * 1000L;
        long now = System.currentTimeMillis();
        if (skewMs > 0 && Math.abs(now - timestamp) > skewMs) {
            log.warn("CA 推送回调时间戳超出偏差 ip={} ts={} now={}", request.getRemoteAddr(), timestamp, now);
            writeError(response, 401, "CA 推送时间戳超出允许偏差");
            return;
        }

        // 必须读原始字节：签名覆盖的是报文原样字节，不能先反序列化再重算
        byte[] body = request.getInputStream().readAllBytes();
        String canonical = tsRaw.trim() + "\n" + nonce.trim() + "\n" + PushSignUtil.sha256Hex(body);
        String expected = PushSignUtil.hmacHex(secret, canonical);
        if (!PushSignUtil.constantTimeEquals(expected, signature.trim().toLowerCase())) {
            log.warn("CA 推送回调验签失败 ip={} uri={}", request.getRemoteAddr(), request.getServletPath());
            writeError(response, 401, "CA 推送签名无效");
            return;
        }

        if (isReplay(nonce.trim(), now, skewMs)) {
            log.warn("CA 推送回调 nonce 重放 ip={} nonce={}", request.getRemoteAddr(), nonce);
            writeError(response, 401, "CA 推送已被接收过（nonce 重复）");
            return;
        }

        // 传下去的是带缓存的请求：控制器仍要能 @RequestBody 读到同一份字节
        filterChain.doFilter(new CachedBodyRequest(request, body), response);
    }

    /**
     * nonce 去重：同一 nonce 在偏差窗口内重复出现即判定为重放。
     */
    private boolean isReplay(String nonce, long now, long ttlMs) {
        long expireAt = now + Math.max(ttlMs, 60_000L);
        if (nonces.size() > PURGE_THRESHOLD) {
            purgeExpired(now);
        }
        Long previous = nonces.put(nonce, expireAt);
        return previous != null && previous > now;
    }

    /**
     * 清理已过期的 nonce：只留还在有效期内的项。
     */
    private void purgeExpired(long now) {
        Iterator<Map.Entry<String, Long>> it = nonces.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() <= now) {
                it.remove();
            }
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(status, message)));
    }

    /**
     * 请求体已读过的包装：把原始字节回放给下游，保证控制器还能正常反序列化。
     */
    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream in = new ByteArrayInputStream(body);
            return new ServletInputStream() {

                @Override
                public boolean isFinished() {
                    return in.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    // 同步读取场景，不需要异步回调
                }

                @Override
                public int read() {
                    return in.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
