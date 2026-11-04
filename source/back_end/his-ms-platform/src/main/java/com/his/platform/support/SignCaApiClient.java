package com.his.platform.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.platform.config.SignProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * csssl 认证中心对外 API 客户端（在线证书申请 RA）。
 *
 * <p>鉴权：HMAC-SHA256 覆盖 appId、时间戳、随机数、方法、路径（含查询串）与请求体哈希；
 * 写请求体再套一层 AES-256-GCM 整包加密（密钥由接入密钥派生），先加密后签名。
 * CA 按 App ID 反查租户，URL 与报文都不携带 tenantKey。</p>
 *
 * <p>本类是 HIS 侧唯一知道 CA 报文形状的地方：业务层只拿到 {@link CaApplication} 等记录对象。</p>
 */
@Slf4j
@Component
public class SignCaApiClient {

    private static final String HEADER_APP_ID = "X-App-Id";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";
    private static final String BODY_KDF_LABEL = "|csssl-api-body-enc-v1";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SignProperties signProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final SecureRandom random = new SecureRandom();

    public SignCaApiClient(SignProperties signProperties, ObjectMapper objectMapper) {
        this.signProperties = signProperties;
        this.objectMapper = objectMapper;
        SignProperties.Ca ca = signProperties.getCa();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(ca.getConnectTimeoutMs()))
                .build();
    }

    private static String hmacHex(String secret, String canonical) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return hex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }

    private static String sha256Hex(byte[] data) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(data));
    }

    // ---------------- 业务接口 ----------------

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static LocalDateTime time(JsonNode node, String field) {
        String value = text(node, field);
        if (!TextUtil.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim().replace('T', ' '), TS);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------- 报文收发 ----------------

    /**
     * 当前是否已接入：enabled 决定「是否接入」；凭证缺失时逐条报出缺了哪几项，不让调用方自己猜
     */
    public void requireEnabled() {
        SignProperties.Ca ca = signProperties.getCa();
        if (!ca.isEnabled()) {
            throw new BusinessException("HIS 未启用 CA 在线申请（his.sign.ca.enabled=false），"
                    + "请改用「生成密钥对 → 线下送 CA 签发 → 回填证书」人工流程");
        }
        String missing = ca.missingCredentialParams();
        if (missing != null) {
            throw new BusinessException("CA 在线申请已启用，但接入凭证未配置：" + missing
                    + "（由 CA 侧建租户时颁发）；请补全后再提交在线申请");
        }
    }

    public CaApplication submit(CaSubmitPayload payload) {
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new BusinessException("构造 CA 申请报文失败：" + e.getMessage());
        }
        return toApplication(call("POST", path(signProperties.getCa().getPaths().getApplySign(), "apply-sign"), body));
    }

    // ---------------- 加解密与签名 ----------------

    /**
     * 撤销申请：申请单号放在请求体内，URL 不暴露参数。
     */
    public CaApplication cancel(String applyNo, String reason) {
        String body;
        try {
            body = objectMapper.writeValueAsString(new CaCancelPayload(applyNo, reason));
        } catch (Exception e) {
            throw new BusinessException("构造 CA 撤销报文失败：" + e.getMessage());
        }
        return toApplication(call("POST", path(signProperties.getCa().getPaths().getCancel(), "cancel"), body));
    }

    /**
     * 吊销已签发证书：证书序列号放在请求体内。
     */
    public void revoke(String serialHex, String reason) {
        if (!TextUtil.hasText(serialHex)) {
            throw new BusinessException("证书序列号为空，无法推送 CA 吊销");
        }
        String body;
        try {
            body = objectMapper.writeValueAsString(new CaRevokePayload(serialHex, reason));
        } catch (Exception e) {
            throw new BusinessException("构造 CA 吊销报文失败：" + e.getMessage());
        }
        call("POST", path(signProperties.getCa().getPaths().getRevoke(), "revoke"), body);
    }

    /**
     * 取配置的接口路径模板；漏配即报错并指出具体的 his.sign.ca.paths.* 键，不用代码里的默认路径兜底
     */
    private String path(String configured, String name) {
        if (!TextUtil.hasText(configured)) {
            throw new BusinessException("CA 接口路径未配置：his.sign.ca.paths." + name);
        }
        return configured;
    }

    private JsonNode call(String method, String pathAndQuery, String plainJson) {
        HttpResponse<String> resp = send(method, pathAndQuery, plainJson);
        if (resp.statusCode() / 100 != 2) {
            throw new BusinessException(errorMessage(resp));
        }
        try {
            JsonNode root = objectMapper.readTree(resp.body());
            int code = root.path("code").asInt(500);
            if (code != 200) {
                throw new BusinessException("CA 返回失败（code=" + code + "）：" + root.path("message").asText());
            }
            return root.path("data");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("CA 响应不是预期的 JSON：" + abbreviate(resp.body()));
        }
    }

    private HttpResponse<String> send(String method, String pathAndQuery, String plainJson) {
        requireEnabled();
        SignProperties.Ca ca = signProperties.getCa();
        String base = ca.getBaseUrl().endsWith("/")
                ? ca.getBaseUrl().substring(0, ca.getBaseUrl().length() - 1) : ca.getBaseUrl();
        URI uri = URI.create(base + pathAndQuery);
        String canonicalPath = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());

        long start = System.currentTimeMillis();
        log.warn("调用 CA 接口地址为：{}", uri);
        String body = "";
        try {
            if (plainJson != null) {
                body = encryptEnvelope(ca.getAppSecret(), plainJson);
            }
            String ts = String.valueOf(System.currentTimeMillis());
            String nonce = UUID.randomUUID().toString().replace("-", "");
            String canonical = ca.getAppId() + "\n" + ts + "\n" + nonce + "\n"
                    + method + "\n" + canonicalPath + "\n" + sha256Hex(body.getBytes(StandardCharsets.UTF_8));
            String signature = hmacHex(ca.getAppSecret(), canonical);

            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(ca.getRequestTimeoutMs()))
                    .header(HEADER_APP_ID, ca.getAppId())
                    .header(HEADER_TIMESTAMP, ts)
                    .header(HEADER_NONCE, nonce)
                    .header(HEADER_SIGNATURE, signature);
            if (plainJson == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json;charset=UTF-8")
                        .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            }
            HttpResponse<String> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            // 无论成功失败都打出响应概览：排查「调用不通 / CA 解密报错」时，能直接看到 CA 到底回了什么
            String preview = resp.body() == null ? "" : (resp.body().length() <= 500 ? resp.body() : resp.body().substring(0, 500) + "...");
            log.warn("CA 接口响应 method={} uri={} status={} cost={}ms bodyPreview={}", method, uri, resp.statusCode(), System.currentTimeMillis() - start, preview);
            return resp;
        } catch (BusinessException e) {
            throw e;
        } catch (java.net.http.HttpTimeoutException e) {
            throw new BusinessException("CA 认证中心响应超时（" + ca.getRequestTimeoutMs() + "ms）："
                    + base + "，请确认 CA 服务状态后重试");
        } catch (java.net.ConnectException e) {
            throw new BusinessException("CA 认证中心不可达：" + base + "（连接被拒绝，请确认服务已启动且地址正确）");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("CA 请求被中断，请重试");
        } catch (Exception e) {
            log.warn("调用 CA 接口失败 method={} path={} err={}", method, pathAndQuery, e.toString());
            throw new BusinessException("调用 CA 接口失败：" + e.getMessage());
        }
    }

    // ---------------- 响应映射 ----------------

    /**
     * 业务错误透传：只标注 HTTP 状态码这一事实，把 CA 响应体里的业务码、错误分类（error / errorCode / errorType）
     * 与 message 原样透出，绝不再替 CA 猜测具体原因。
     *
     * <p>旧版把 403 一律写成"来源地址不在白名单或证书校验未通过"，而 CA 实际返回的 message 可能是
     * "公司不可用"——两者自相矛盾，误导排查（以为问题在白名单，其实是公司被停用）。错误的真实分类
     * 由 CA 决定，HIS 不应二度猜测，直接把 CA 给的字段亮出来最准。</p>
     */
    private String errorMessage(HttpResponse<String> resp) {
        int status = resp.statusCode();
        String rawBody = resp.body();
        Integer bizCode = null;
        String message = null;
        String errorType = null;
        boolean json = false;
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            if (root != null && !root.isMissingNode() && !root.isNull()) {
                json = true;
                if (root.path("code").isInt()) {
                    bizCode = root.path("code").asInt();
                }
                message = root.path("message").asText(null);
                // CA 若用结构化字段标注具体错误分类，原样取出，不二次猜测
                if (root.path("error").isTextual()) {
                    errorType = root.path("error").asText();
                } else {
                    errorType = root.path("errorCode").asText(null);
                    if (errorType == null) {
                        errorType = root.path("errorType").asText(null);
                    }
                }
            }
        } catch (Exception ignored) {
            // 非 JSON 错误页（网关/代理/CA 自身崩溃）按状态码兜底
        }
        // CA 崩了返回 500 错误页（含堆栈/异常名）时，把异常线索原样带出来，
        // 否则只会显示「CA 返回 HTTP 500」，看不到「私钥解密失败(BadPaddingException)」这类根因
        if (!json && message == null) {
            message = extractExceptionHint(rawBody);
        }
        String prefix = switch (status) {
            case 401 -> "CA 认证失败（App ID 或接入密钥不正确）";
            case 403 -> "CA 拒绝访问（HTTP 403）";
            case 409 -> "CA 申请状态冲突";
            case 429 -> "CA 请求过于频繁，请稍后重试";
            case 503 -> "CA 对外 API 暂未开放";
            default -> "CA 返回 HTTP " + status;
        };
        StringBuilder sb = new StringBuilder(prefix);
        if (bizCode != null) {
            sb.append("（业务码 ").append(bizCode).append("）");
        }
        if (TextUtil.hasText(errorType)) {
            sb.append(" [").append(errorType).append("]");
        }
        if (TextUtil.hasText(message)) {
            sb.append("：").append(message);
        }
        return sb.toString();
    }

    /**
     * 从非 JSON 错误页/堆栈里抓第一个异常线索（异常类名 + 简短信息），便于排查 CA 侧崩溃。
     * 例如 CA 返回 500 且页面含 "IllegalStateException: 私钥解密失败...BadPaddingException" 时，
     * 能直接把这段带出来，而不是只给一个笼统的「CA 返回 HTTP 500」。
     */
    private static String extractExceptionHint(String body) {
        if (!TextUtil.hasText(body)) {
            return null;
        }
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "(?:[a-zA-Z_][\\w.]*(?:Exception|Error))[^\\n<>]{0,160}");
        java.util.regex.Matcher m = p.matcher(body);
        return m.find() ? m.group().trim() : null;
    }

    private String encryptEnvelope(String secret, String plainJson) throws Exception {
        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest((secret + BODY_KDF_LABEL).getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[IV_LEN];
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
        byte[] encrypted = cipher.doFinal(plainJson.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        String encData = Base64.getEncoder().encodeToString(combined);
        return objectMapper.createObjectNode().put("encData", encData).toString();
    }

    private CaApplication toApplication(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            throw new BusinessException("CA 未返回申请单信息");
        }
        CaCertificate certificate = null;
        JsonNode certNode = node.path("certificate");
        if (certNode.isObject()) {
            certificate = new CaCertificate(
                    text(certNode, "serialHex"), text(certNode, "subjectDn"), text(certNode, "issuerDn"),
                    text(certNode, "algorithm"), time(certNode, "notBefore"), time(certNode, "notAfter"),
                    text(certNode, "fingerprintSha256"), text(certNode, "certPem"), text(certNode, "chainPem"));
        }
        return new CaApplication(
                text(node, "applyNo"), text(node, "externalRequestId"), text(node, "status"),
                text(node, "statusText"), time(node, "submittedAt"), time(node, "reviewedAt"),
                text(node, "reviewedByName"), text(node, "reviewComment"), time(node, "issuedAt"),
                certificate, node.path("idempotentReplay").asBoolean(false));
    }

    // ---------------- 报文记录 ----------------

    /**
     * 申请提交报文（字段名与 CA 契约逐字一致）
     */
    public record CaSubmitPayload(String externalRequestId, String applicantType, String applicantName,
                                  String subjectDn, String templateCode, String publicKeyPem,
                                  String san, String description, String externalEmployeeId,
                                  String externalEmployeeCode, String externalDeptName) {
    }

    public record CaCancelPayload(String applyNo, String reason) {
    }

    public record CaRevokePayload(String serialHex, String reason) {
    }

    public record CaCertificate(String serialHex, String subjectDn, String issuerDn, String algorithm,
                                LocalDateTime notBefore, LocalDateTime notAfter,
                                String fingerprintSha256, String certPem, String chainPem) {
    }

    public record CaApplication(String applyNo, String externalRequestId, String status, String statusText,
                                LocalDateTime submittedAt, LocalDateTime reviewedAt, String reviewedByName,
                                String reviewComment, LocalDateTime issuedAt, CaCertificate certificate,
                                boolean idempotentReplay) {
    }
}
