package com.his.platform.filter;

import com.csssl.sdk.CaPushVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.base.Result;
import com.his.common.util.TextUtil;
import com.his.platform.config.SignProperties;
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
 * CA 推送回调验签：用 csssl-push-sdk 的 CaPushVerifier 做 SM2 非对称验签。
 *
 * <p>校验 {@code X-Ca-Timestamp} / {@code X-Ca-Nonce} / {@code X-Ca-Signature} / {@code X-Ca-Key-Id}。
 * CA 侧用推送私钥签，HIS 用推送公钥验——公钥是公开的，泄露也只能验签不能伪造。</p>
 *
 * <p>公钥未配置时<b>拒绝一切推送</b>：回调端点匿名开放，无验签等于允许任意伪造签发结果。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class SignCaCallbackSignatureFilter extends OncePerRequestFilter {

    private static final String CALLBACK_PATH = "/emr/signCert/caCallback";

    private static final String HEADER_TIMESTAMP = "X-Ca-Timestamp";
    private static final String HEADER_NONCE = "X-Ca-Nonce";
    private static final String HEADER_SIGNATURE = "X-Ca-Signature";
    private static final String HEADER_KEY_ID = "X-Ca-Key-Id";

    private static final int PURGE_THRESHOLD = 4096;

    private final SignProperties signProperties;
    private final ObjectMapper objectMapper;

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
        String publicKeyHex = ca.getCallbackPublicKey();
        if (!TextUtil.hasText(publicKeyHex)) {
            log.error("CA 推送回调未配置 his.sign.ca.callback-public-key，已拒绝推送；"
                    + "该端点匿名开放，无验签等于允许任意伪造签发结果");
            writeError(response, 503, "CA 推送回调未配置验签公钥，服务端拒绝接收");
            return;
        }

        String tsRaw = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String signature = request.getHeader(HEADER_SIGNATURE);
        String keyId = request.getHeader(HEADER_KEY_ID);
        if (!TextUtil.hasText(tsRaw) || !TextUtil.hasText(nonce) || !TextUtil.hasText(signature)) {
            log.warn("CA 推送回调缺少签名头 ip={} uri={}", request.getRemoteAddr(), request.getServletPath());
            writeError(response, 401, "CA 推送缺少签名头");
            return;
        }

        // 校验 keyId（如果配置了预期值）
        String expectedKeyId = ca.getCallbackKeyId();
        if (TextUtil.hasText(expectedKeyId) && !expectedKeyId.equals(keyId)) {
            log.warn("CA 推送回调 keyId 不匹配 ip={} expected={} actual={}",
                    request.getRemoteAddr(), expectedKeyId, keyId);
            writeError(response, 401, "CA 推送 keyId 不匹配");
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

        // 用 SDK 验签
        CaPushVerifier verifier = new CaPushVerifier(publicKeyHex);
        boolean valid = verifier.verify(tsRaw, nonce, body, signature);
        if (!valid) {
            log.warn("CA 推送回调验签失败 ip={} uri={}", request.getRemoteAddr(), request.getServletPath());
            writeError(response, 401, "CA 推送签名无效");
            return;
        }

        if (isReplay(nonce.trim(), now, skewMs)) {
            log.warn("CA 推送回调 nonce 重放 ip={} nonce={}", request.getRemoteAddr(), nonce);
            writeError(response, 401, "CA 推送已被接收过（nonce 重复）");
            return;
        }

        filterChain.doFilter(new CachedBodyRequest(request, body), response);
    }

    private boolean isReplay(String nonce, long now, long ttlMs) {
        long expireAt = now + Math.max(ttlMs, 60_000L);
        if (nonces.size() > PURGE_THRESHOLD) {
            purgeExpired(now);
        }
        Long previous = nonces.put(nonce, expireAt);
        return previous != null && previous > now;
    }

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
