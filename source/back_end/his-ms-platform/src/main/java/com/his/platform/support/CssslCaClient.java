package com.his.platform.support;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.SM2;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.exception.BusinessException;
import com.his.platform.config.SignProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * csssl 院内 CA 的 REST 客户端（RA 通道）：
 * 服务账号登录（SM2 口令加密，协议对齐 csssl/cslk）→ 公钥签发（私钥自持，仅提交 Subject DN + 公钥）→ 取证书链。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CssslCaClient {

    /** 签发证书的申请类型：CLIENT 证书带 clientAuth EKU，用于医生签名 */
    private static final String CERT_TYPE_SIGN = "CLIENT";

    /** token 缓存提前过期（秒），避免临界窗口用废 token */
    private static final long TOKEN_REUSE_SECONDS = 23 * 3600;

    /**
     * 证书下载路径配置里的证书主键占位符，发出前替换为签发回执返回的 id
     */
    private static final String CERT_ID_PLACEHOLDER = "{certId}";

    private final SignProperties signProperties;
    private final ObjectMapper objectMapper;

    private volatile String cachedToken;
    private volatile long tokenExpireAt;

    /** 证书签发结果：证书链 PEM / CA 序列号 / 签发 CA 名称 */
    public record IssuedCert(String certPem, String caSerial, String caIssuer) {
    }

    /** csssl 时间戳盖章结果（对齐外部 TSA 盖章回执） */
    public record TsaStamp(String serial, String tsaTime, String tokenValue, String algo, String fingerprint) {
    }

    /** 向 csssl 提交公钥签发申请并取回证书链 */
    public IssuedCert issue(String subjectDn, String publicKeyPem, int validDays) {
        String token = token();

        // 1) 提交公钥签发
        Map<String, Object> req = new java.util.LinkedHashMap<>();
        req.put("subjectDn", subjectDn);
        req.put("publicKeyPem", publicKeyPem);
        req.put("certType", CERT_TYPE_SIGN);
        req.put("validityDays", validDays);
        JsonNode issueResp = post(caPath(signProperties.getCaPaths().getCertIssue()), token, req);
        JsonNode cert = issueResp.path("data");
        if (!cert.isObject() || !cert.hasNonNull("id")) {
            throw new BusinessException("外部 CA 签发回执异常：未返回证书信息（" + brief(issueResp) + "）");
        }
        long certId = cert.get("id").asLong();
        String serialHex = cert.get("serialHex").asText();
        String issuerCn = cert.get("issuerCn") == null ? "" : cert.get("issuerCn").asText();

        // 2) 取证书链 PEM（CertVo 不带证书内容，单独走下载接口）
        String certPem = getText(caPath(signProperties.getCaPaths().getCertDownload())
                .replace(CERT_ID_PLACEHOLDER, String.valueOf(certId)), token);
        if (certPem == null || !certPem.startsWith("-----BEGIN CERTIFICATE-----")) {
            throw new BusinessException("外部 CA 返回的证书内容异常（下载证书接口未返回 PEM）");
        }
        log.info("[csssl CA] 签发成功 serial={} issuer={} cn={}", serialHex, issuerCn, subjectDn);
        return new IssuedCert(certPem, serialHex, issuerCn);
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

    /** 登录令牌（缓存复用，过期或 401 时重新登录） */
    private String token() {
        long now = System.currentTimeMillis() / 1000;
        if (cachedToken != null && tokenExpireAt > now) {
            return cachedToken;
        }
        synchronized (this) {
            if (cachedToken != null && tokenExpireAt > System.currentTimeMillis() / 1000) {
                return cachedToken;
            }
            String token = login();
            cachedToken = token;
            tokenExpireAt = System.currentTimeMillis() / 1000 + TOKEN_REUSE_SECONDS;
            return token;
        }
    }

    /** 服务账号登录：SM2 口令加密（C1C3C2，不带 04 前缀，协议对齐 csssl 登录） */
    private String login() {
        String username = signProperties.getCaUsername();
        String password = signProperties.getCaPassword();
        if (!hasText(username) || !hasText(password)) {
            throw new BusinessException("外部 CA 服务账号未配置（his.sign.ca-username / ca-password）");
        }
        JsonNode pkResp = get(caPath(signProperties.getCaPaths().getLoginPublicKey()));
        String publicKeyHex = pkResp.path("data").path("publicKey").asText("");
        if (!hasText(publicKeyHex)) {
            throw new BusinessException("外部 CA 公钥获取失败：" + brief(pkResp));
        }
        String cipherHex = sm2Encrypt(publicKeyHex, password);
        JsonNode loginResp = post(caPath(signProperties.getCaPaths().getLogin()), null,
                Map.of("username", username, "passwordCipher", cipherHex));
        String token = loginResp.path("data").path("token").asText("");
        if (!hasText(token)) {
            throw new BusinessException("外部 CA 登录失败：" + brief(loginResp));
        }
        return token;
    }

    /** hutool SM2 公钥加密 → 十六进制（对齐 csssl 期望：C1C3C2、不带 04 前缀） */
    private String sm2Encrypt(String publicKeyHex, String plain) {
        try {
            SM2 sm2 = new SM2(null, publicKeyHex);
            sm2.setMode(SM2Engine.Mode.C1C3C2);
            byte[] encrypted = sm2.encrypt(plain.getBytes(StandardCharsets.UTF_8));
            String hex = HexUtil.encodeHexStr(encrypted);
            return hex.startsWith("04") ? hex.substring(2) : hex;
        } catch (Exception e) {
            throw new BusinessException("SM2 口令加密失败：" + e.getMessage());
        }
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

    private String getText(String url, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        try {
            HttpResponse<String> resp = client().send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new BusinessException("外部 CA 请求失败：HTTP " + resp.statusCode() + " " + resp.body());
            }
            return resp.body();
        } catch (java.io.IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("外部 CA 请求异常：" + e.getMessage());
        }
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
