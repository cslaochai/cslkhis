package com.his.platform.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.exception.BusinessException;
import com.his.platform.config.SignProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * csssl 院内 CA 的 REST 客户端（TSA 通道）。
 *
 * <p>只保留 TSA 相关能力：盖章与公钥获取（均匿名访问，无需登录）。
 * 证书签发不再走接口——HIS 与 CA 认证中心的交互改为线下人工（申请单 + 回填证书），
 * 私钥自始至终留在 HIS，不经本客户端。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CssslCaClient {

    private final SignProperties signProperties;

    private final ObjectMapper objectMapper;

    /**
     * csssl 时间戳盖章结果（对齐外部 TSA 盖章回执）
     */
    public record TsaStamp(String serial, String tsaTime, String tokenValue, String algo, String fingerprint) {
    }

    /** 向 csssl TSA 盖一枚时间戳令牌（摘要 → 序列号+时刻+令牌；TSA 接口匿名，无需登录） */
    public TsaStamp stampTsa(String digestHex) {
        JsonNode resp = post(caPath(signProperties.getCaPaths().getTsaStamp()), null,
                Map.of("digestHex", digestHex));
        JsonNode data = resp.path("data");
        if (!data.isObject() || !data.hasNonNull("serial") || !data.hasNonNull("tokenValue")) {
            throw new BusinessException("外部 TSA 盖章回执异常：" + brief(resp));
        }
        return new TsaStamp(data.get("serial").asText(),
                data.get("tsaTime") == null ? "" : data.get("tsaTime").asText(),
                data.get("tokenValue").asText(),
                data.get("algo") == null ? "" : data.get("algo").asText(),
                data.get("publicKeyFingerprint") == null ? "" : data.get("publicKeyFingerprint").asText());
    }

    /** 取 csssl TSA 公钥（PEM + SM3 指纹），用于本地验签令牌 */
    public Map<String, String> tsaPublicKey() {
        JsonNode resp = get(caPath(signProperties.getCaPaths().getTsaPublicKey()));
        JsonNode data = resp.path("data");
        if (!data.isObject() || !data.hasNonNull("publicKeyPem")) {
            throw new BusinessException("外部 TSA 公钥获取失败：" + brief(resp));
        }
        Map<String, String> out = new java.util.LinkedHashMap<>();
        out.put("publicKeyPem", data.get("publicKeyPem").asText());
        out.put("fingerprint", data.get("fingerprint") == null ? "" : data.get("fingerprint").asText());
        out.put("algorithm", data.get("algorithm") == null ? "" : data.get("algorithm").asText());
        return out;
    }

    // ---------- HTTP 基础 ----------

    private String baseUrl() {
        String url = signProperties.getCaBaseUrl();
        if (!hasText(url)) {
            throw new BusinessException("外部 CA 地址未配置（his.sign.ca-base-url，如 http://localhost:8092/ssl）");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** 配置的接口路径拼上 CA 根地址 */
    private String caPath(String path) {
        return baseUrl() + path;
    }

    private HttpClient client() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(signProperties.getCaTimeoutMs(), 1000)))
                .build();
    }

    private JsonNode get(String url) {
        return send(url, null, null);
    }

    private JsonNode post(String url, String token, Map<String, Object> body) {
        return send(url, token, body);
    }

    private JsonNode send(String url, String token, Map<String, Object> body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(writeJson(body), StandardCharsets.UTF_8));
        } else {
            builder.GET();
        }
        try {
            HttpResponse<String> resp = client().send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new BusinessException("外部 CA 请求失败：HTTP " + resp.statusCode() + " " + resp.body());
            }
            return objectMapper.readTree(resp.body());
        } catch (java.io.IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("外部 CA 请求异常：" + e.getMessage());
        }
    }

    private String writeJson(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new BusinessException("外部 CA 请求序列化失败：" + e.getMessage());
        }
    }

    private String brief(JsonNode node) {
        return node == null ? "空响应" : node.toString().length() > 120 ? node.toString().substring(0, 120) + "..." : node.toString();
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
