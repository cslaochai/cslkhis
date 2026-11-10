package com.his.ca.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.ca.sdk.dto.CaApiResponse;
import com.his.ca.sdk.dto.CaApplyRequest;
import com.his.ca.sdk.dto.CaApplyResult;
import com.his.ca.sdk.dto.CaRevokeRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * CA 对外接口客户端：HIS / EMR / LIS 等系统依赖 SDK 后直接调，
 * 不用自己拼报文、自己算签名。
 *
 * <p>鉴权走 SM2 非对称签名：本地持私钥签名（{@code X-Signature}），
 * CA 侧用登记的公钥验签。私钥永远不出本机，CA 也不需要保存任何共享口令。</p>
 *
 * <pre>{@code
 * // 1) 首次接入：本地生成一对 SM2 密钥，把公钥登记到 CA
 * CaApiClient client = CaApiClient.builder()
 *         .baseUrl("https://ca.example.com/ssl")
 *         .appId("app_xxx")
 *         .privateKeyHex("4c2f...")   // 私钥只放本机配置/密钥库
 *         .build();
 *
 * // 2) 调用
 * CaApplyResult result = client.applySign(request);
 * }</pre>
 *
 * <p>纯 Java 实现，不依赖 Spring。</p>
 */
public class CaApiClient {

    private static final String HEADER_APP_ID = "X-App-Id";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";

    private final String baseUrl;
    private final String appId;
    private final String privateKeyHex;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Paths paths;

    private CaApiClient(Builder b) {
        this.baseUrl = b.baseUrl.endsWith("/") ? b.baseUrl.substring(0, b.baseUrl.length() - 1) : b.baseUrl;
        this.appId = b.appId;
        this.privateKeyHex = b.privateKeyHex;
        this.objectMapper = new ObjectMapper();
        this.paths = b.paths;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(b.connectTimeoutMs))
                .build();
    }

    /**
     * 提交证书申请。
     */
    public CaApplyResult applySign(CaApplyRequest req) throws Exception {
        String body = objectMapper.writeValueAsString(req);
        HttpResponse<String> resp = post(paths.applySign, body);
        return parse(resp, CaApplyResult.class);
    }

    /**
     * 撤销申请（申请单号放在请求体内，URL 不暴露参数）。
     */
    public CaApplyResult cancel(String applyNo, String reason) throws Exception {
        String body = objectMapper.writeValueAsString(new CancelRequest(applyNo, reason));
        HttpResponse<String> resp = post(paths.cancel, body);
        return parse(resp, CaApplyResult.class);
    }

    /**
     * 吊销已签发证书。
     */
    public CaApplyResult revoke(String certSerial, String operatorName, String reason) throws Exception {
        CaRevokeRequest req = new CaRevokeRequest();
        req.setCertSerial(certSerial);
        req.setOperatorName(operatorName);
        req.setReason(reason);
        String body = objectMapper.writeValueAsString(req);
        HttpResponse<String> resp = post(paths.revoke, body);
        return parse(resp, CaApplyResult.class);
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString().replace("-", "");
        // 签名串里的路径必须与 CA 端 request.getRequestURI() 完全一致，
        // 即要带上 baseUrl 里的 context-path（如 /ssl），否则 CA 算出的是另一条串，必然 401
        String signedPath = URI.create(baseUrl + path).getRawPath();
        String canonical = CaRequestSigner.canonical(
                appId, timestamp, nonce, "POST", signedPath, CaRequestSigner.sha256Hex(bodyBytes));
        String signature = CaRequestSigner.sign(privateKeyHex, canonical);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json;charset=UTF-8")
                .header(HEADER_APP_ID, appId)
                .header(HEADER_TIMESTAMP, timestamp)
                .header(HEADER_NONCE, nonce)
                .header(HEADER_SIGNATURE, signature)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private <T> T parse(HttpResponse<String> resp, Class<T> type) throws Exception {
        if (resp.statusCode() / 100 != 2) {
            throw new CaApiException("CA 接口返回错误: " + resp.statusCode() + " " + abbreviate(resp.body()));
        }
        CaApiResponse<T> r = objectMapper.readValue(resp.body(),
                objectMapper.getTypeFactory().constructParametricType(CaApiResponse.class, type));
        if (!r.isSuccess()) {
            throw new CaApiException("CA 接口业务错误: " + r.getMessage());
        }
        return r.getData();
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 200 ? text : text.substring(0, 200) + "...";
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * CA 接口路径（默认与 CA 服务端一致，可覆盖）。
     */
    public static class Paths {
        private String applySign = "/ca/api/v1/apply/sign";
        private String cancel = "/ca/api/v1/apply/cancel";
        private String revoke = "/ca/api/v1/certificates/revoke";

        public Paths applySign(String path) { this.applySign = path; return this; }
        public Paths cancel(String path) { this.cancel = path; return this; }
        public Paths revoke(String path) { this.revoke = path; return this; }
    }

    public static class Builder {
        private String baseUrl;
        private String appId;
        private String privateKeyHex;
        private int connectTimeoutMs = 5000;
        private Paths paths = new Paths();

        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder appId(String appId) { this.appId = appId; return this; }
        public Builder privateKeyHex(String privateKeyHex) { this.privateKeyHex = privateKeyHex; return this; }
        public Builder connectTimeoutMs(int ms) { this.connectTimeoutMs = ms; return this; }
        public Builder paths(Paths paths) { this.paths = paths; return this; }

        public CaApiClient build() {
            if (baseUrl == null || baseUrl.isBlank()
                    || appId == null || appId.isBlank()
                    || privateKeyHex == null || privateKeyHex.isBlank()) {
                throw new IllegalStateException("baseUrl / appId / privateKeyHex 不能为空");
            }
            return new CaApiClient(this);
        }
    }

    /** 内部撤销请求 DTO */
    record CancelRequest(String applyNo, String reason) {}

    /** CA 接口异常 */
    public static class CaApiException extends RuntimeException {
        public CaApiException(String msg) { super(msg); }
    }
}
