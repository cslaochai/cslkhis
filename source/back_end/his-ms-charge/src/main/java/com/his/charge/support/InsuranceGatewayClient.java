package com.his.charge.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.charge.config.InsuranceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 医保前置系统（csmig）报文客户端：HTTP 传输 + CSSSL-SEC-V1 国密信封 + 回执按交易码绑定类型。
 *
 * <p>只回答「一份业务报文怎么送出去、回执怎么还原成有类型的业务对象」，不含参保核验/结算拆分等判定——
 * 那些留在通道实现里，由它声明交易码对应的入参与出参类型。
 *
 * <p>信封收发委托 {@link InsuranceMessageCrypto}：国密开关开着时，发出前 SM4 加密 + SM2 加签，
 * 回执先验签解密再绑类型；日对账按前置机约定走 GET 明文，不进信封。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "his.insurance.channel.mode", havingValue = "remote")
public class InsuranceGatewayClient {

    /**
     * 报文含中文（患者姓名、诊断、科室），String 报文必须显式 UTF-8，避免按默认字符集发成乱码
     */
    private static final MediaType JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    /**
     * 前置机统一响应体的成功码（对齐对方 Result 包装，非业务码值）
     */
    private static final Integer CODE_OK = 200;

    private final ObjectMapper objectMapper;
    private final InsuranceProperties properties;
    private final RestClient restClient;
    private volatile InsuranceMessageCrypto crypto;

    public InsuranceGatewayClient(ObjectMapper objectMapper, InsuranceProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        InsuranceProperties.Channel channel = properties.getChannel();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(channel.getConnectTimeoutMs());
        factory.setReadTimeout(channel.getReadTimeoutMs());
        this.restClient = RestClient.builder().baseUrl(channel.getBaseUrl()).requestFactory(factory).build();
        log.info("[医保通道] 已启用远程医保结算经办平台 HTTP 通道：{}，报文国密：{}",
                channel.getBaseUrl(),
                properties.getCrypto().isEnabled() ? "SM2签名+SM4加密（csssl证书）" : "明文（开发期）");
    }

    /**
     * 上送业务报文并取回类型化回执。
     *
     * @param path            相对前置机根地址的接口路径（交易码已由调用方拼好）
     * @param businessPayload 业务报文对象；已是 JSON 字符串（2304 报盘原文）时原样送出
     * @param dataType        回执 {@code data} 段的类型，与本交易码一一对应
     * @throws TransportException 传输层失败（请求是否已达前置机由 uncertain 区分）
     * @throws EnvelopeException  国密开启时回执验签/解密未通过
     * @throws ReplyException     回执不是合法 JSON 或与 dataType 绑不上
     */
    public <T> InsReply<T> post(String path, Object businessPayload, Class<T> dataType) {
        String businessJson = asBusinessJson(businessPayload);
        log.info("[医保通道] POST {} 请求报文：{}", path, businessJson);
        String rawResponse = postForRawBody(path, businessJson);
        return parseReply(unwrapEnvelope(path, rawResponse), dataType);
    }

    /**
     * 日对账账单拉取：前置机对该接口走 GET 明文（不加签不加密），故不经信封。
     *
     * @param path     日对账路径
     * @param billDate 账期
     * @return 平台侧「对方账」行；回执没有 data 数组时返回空列表
     * @throws TransportException 传输层失败（对账必须让使用方感知平台不可用）
     * @throws ReplyException     回执不是合法 JSON
     */
    public <T> List<T> getForList(String path, LocalDate billDate, Class<T> itemType) {
        String rawResponse;
        try {
            rawResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path(path).queryParam("billDate", billDate.toString()).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            log.error("[医保通道] GET {} 失败 billDate={}：{}", path, billDate, e.getMessage());
            throw new TransportException(isUncertainTransport(e), "GET " + path + " 失败：" + e.getMessage(), e);
        }
        log.info("[医保通道] GET {} 响应报文：{}", path, rawResponse);
        try {
            List<T> list = new ArrayList<>();
            objectMapper.readTree(rawResponse).path("data")
                    .forEach(item -> list.add(objectMapper.convertValue(item, itemType)));
            return list;
        } catch (Exception e) {
            throw new ReplyException("GET " + path + " 回执解析失败：" + e.getMessage(), rawResponse, e);
        }
    }

    private String asBusinessJson(Object businessPayload) {
        if (businessPayload instanceof String text) {
            return text;
        }
        try {
            return objectMapper.writeValueAsString(businessPayload);
        } catch (Exception e) {
            throw new IllegalArgumentException("业务报文序列化失败：" + e.getMessage(), e);
        }
    }

    private String postForRawBody(String path, String businessJson) {
        try {
            String rawResponse = restClient.post()
                    .uri(path)
                    .contentType(JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(sendBody(businessJson))
                    .retrieve()
                    .body(String.class);
            log.info("[医保通道] POST {} 响应报文：{}", path, rawResponse);
            return rawResponse;
        } catch (RestClientException e) {
            log.error("[医保通道] POST {} 发送失败 uncertain={}：{}", path, isUncertainTransport(e), e.getMessage());
            throw new TransportException(isUncertainTransport(e), "POST " + path + " 失败：" + e.getMessage(), e);
        }
    }

    /** 国密开启时解安全信封；关闭时原样返回。验签/解密未过转成 {@link EnvelopeException}，带上原始报文供台账 */
    private String unwrapEnvelope(String path, String rawResponse) {
        if (!properties.getCrypto().isEnabled()) {
            return rawResponse;
        }
        try {
            return crypto().unpackEnvelope(rawResponse);
        } catch (IllegalStateException e) {
            log.error("[医保通道] POST {} 回执安全校验失败：{}", path, e.getMessage());
            throw new EnvelopeException(e.getMessage(), rawResponse, e);
        }
    }

    /** 国密开启时发出安全信封；关闭时原样发业务 JSON */
    private String sendBody(String businessJson) {
        if (properties.getCrypto().isEnabled()) {
            return crypto().buildEnvelope(businessJson);
        }
        return businessJson;
    }

    /**
     * 回执包装按 {@code {code, message, data}} 逐字还原，{@code data} 段再绑成 dataType。
     * data 原文单独留一份：报盘台账要存对方给的全文（含本端没声明的列），不能拿绑定结果回序列化。
     */
    private <T> InsReply<T> parseReply(String businessJson, Class<T> dataType) {
        try {
            JsonNode root = objectMapper.readTree(businessJson);
            JsonNode data = root.path("data");
            return new InsReply<>(root.path("code").asInt(-1),
                    root.path("message").asText(null),
                    data.isMissingNode() || data.isNull() ? null : objectMapper.convertValue(data, dataType),
                    businessJson, data.isMissingNode() || data.isNull() ? null : data.toString());
        } catch (Exception e) {
            throw new ReplyException("回执绑定失败（期望 data 为 " + dataType.getSimpleName() + "）："
                    + e.getMessage(), businessJson, e);
        }
    }

    /**
     * 传输层失败分类：只有「请求发出后没有得到任何响应」的失败才算不确定（超时/连接被重置——
     * 医保侧可能已扣账）；ConnectException/UnknownHostException = 请求根本没出去；拿到 HTTP 状态码
     * （RestClientResponseException）= 前置机已响应，业务失败是确定的。
     */
    private boolean isUncertainTransport(RestClientException e) {
        if (e instanceof org.springframework.web.client.RestClientResponseException) {
            return false;
        }
        Throwable c = e.getCause();
        while (c != null) {
            if (c instanceof java.net.ConnectException || c instanceof java.net.UnknownHostException) {
                return false;
            }
            if (c instanceof java.net.SocketTimeoutException || c instanceof java.net.SocketException) {
                return true;
            }
            c = c.getCause();
        }
        return false;
    }

    private InsuranceMessageCrypto crypto() {
        if (crypto == null) {
            synchronized (this) {
                if (crypto == null) {
                    crypto = new InsuranceMessageCrypto(properties.getCrypto());
                    log.info("[医保通道] 报文安全通道就绪（SM4 会话密钥/证书链已加载），时间窗 {}s",
                            properties.getCrypto().getClockSkewSeconds());
                }
            }
        }
        return crypto;
    }

    /** 传输失败：请求是否可能已达医保侧，由调用方决定能否降级 */
    public static class TransportException extends RuntimeException {

        /**
         * true = 请求可能已到医保侧并扣账（超时/连接中断），必须先冲正确认；
         * false = 请求未达或对方已明确响应，可按确定失败处理
         */
        private final boolean uncertain;

        public TransportException(boolean uncertain, String message, Throwable cause) {
            super(message, cause);
            this.uncertain = uncertain;
        }

        public boolean isUncertain() {
            return uncertain;
        }
    }

    /** 回执解析/绑定失败，携带原始报文供台账与排障 */
    public static class ReplyException extends RuntimeException {

        private final String rawBody;

        public ReplyException(String message, String rawBody, Throwable cause) {
            super(message, cause);
            this.rawBody = rawBody;
        }

        public String getRawBody() {
            return rawBody;
        }
    }

    /** 安全信封未过（版本不符/字段缺失/超时间窗/证书链或验签失败），携带原始报文供台账与排障 */
    public static class EnvelopeException extends RuntimeException {

        private final String rawBody;

        public EnvelopeException(String message, String rawBody, Throwable cause) {
            super(message, cause);
            this.rawBody = rawBody;
        }

        public String getRawBody() {
            return rawBody;
        }
    }

    /**
     * 前置机统一响应包装（对齐对方 Result）：一条交易码回执 = 一个 code + 一个类型的 data。
     *
     * @param code        通道层结果码，200 = 前置机正常受理并给出 data
     * @param message     通道层提示，失败时是可直接展示给用户的原因
     * @param data        业务回执，按交易码绑定；查无数据时为 null
     * @param rawBusiness 解密后的响应全文，撤销回执台账留档用
     * @param rawData     响应 {@code data} 段原文，报盘台账留档用（含本端未声明的列）
     */
    public record InsReply<T>(Integer code, String message, T data, String rawBusiness, String rawData) {

        /** 前置机是否正常受理（非 200 一律按失败，失败原因取 message） */
        public boolean accepted() {
            return CODE_OK.equals(code);
        }
    }

    /** 参保查询入参：身份证与医保卡号只带其一，未带的键不序列化（保持报文逐字与改造前一致） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PersonQueryInput(String idCard, String insuranceNo) {
    }

    /** 2305 撤销入参：按原上传流水号冲正医保侧实时结算 */
    public record CancelInput(String msgType, String tradeNo, String origTradeNo, String cancelReason, String sendTime) {
    }

    /** 2201/2301 实时结算回执：统筹/个账/现金三段拆分的事实来源 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SettleData(Integer infcode, String tradeNo, String receiptNo, String insuranceType, String name,
                             BigDecimal total, BigDecimal insurancePay, BigDecimal personalAccount,
                             BigDecimal cashPay, BigDecimal selfPay, BigDecimal deductible,
                             BigDecimal accountBalanceAfter, String warnMsg, String errMsg) {
    }

    /** 2304/2305 报盘与撤销回执 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReceiptData(Integer infcode, String msgType, String tradeNo, String receiptNo,
                              String warnMsg, String errMsg) {

        /** 医保侧接受报文的回执码（非 0 一律按失败，原因看 errMsg） */
        private static final Integer INFCODE_ACCEPTED = 0;

        /** 医保侧是否接受本笔报文（infcode=0 才是接受） */
        public boolean confirmed() {
            return INFCODE_ACCEPTED.equals(infcode);
        }
    }

    /** 参保人回执：结算侧只用姓名、卡号、险种与参保状态做核验 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonData(String name, String insuranceNo, String insuranceType, Integer personStatus) {
    }

    /** 日对账「对方账」单行 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DayBillItem(String tradeNo, String settlementNo, BigDecimal totalAmount,
                              BigDecimal insurancePay, String receiptNo) {
    }
}
