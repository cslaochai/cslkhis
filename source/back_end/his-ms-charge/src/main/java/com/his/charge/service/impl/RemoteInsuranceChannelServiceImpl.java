package com.his.charge.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.charge.config.InsuranceProperties;
import com.his.charge.service.InsuranceChannelService;
import com.his.charge.support.InsuranceMessageCrypto;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 医保前置系统出口（HTTP 实现）：HIS → csmig前置(8091/mig) → csmis医保局端(8090/mss)。
 *
 * <p>报文安全（his.insurance.crypto.enabled=true）：HIS 用 csssl 签发的服务证书（SM2）对报文签名，
 * SM4 加密后发前置；前置回执同样验签解密后再解析业务字段。密钥与前置机 mig.crypto.sm4-key-his 一致，
 * 院内段密钥不出前置机。
 *
 * <p>与本地 mock 实现二选一，由配置 {@code his.insurance.channel.mode=remote} 激活。
 * 上游契约见 csmig 的 GatewayController（/gateway/{msgType}），csmig 透传到 csmis。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "his.insurance.channel.mode", havingValue = "remote")
public class RemoteInsuranceChannelServiceImpl implements InsuranceChannelService {

    /**
     * 2304/2305 报文含中文（患者姓名、诊断、科室），String 报文必须显式 UTF-8，避免按默认字符集发成乱码
     */
    private static final MediaType JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final InsuranceProperties properties;
    private volatile InsuranceMessageCrypto crypto;

    public RemoteInsuranceChannelServiceImpl(
            ObjectMapper objectMapper,
            InsuranceProperties properties) {
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

    /** 国密开启时发出安全信封；关闭时原样发业务 JSON */
    private String sendBody(String businessJson) {
        if (properties.getCrypto().isEnabled()) {
            return crypto().buildEnvelope(businessJson);
        }
        return businessJson;
    }

    /** 国密开启时解安全信封；关闭时原样返回 */
    private String receiveBody(String rawResponse) {
        if (properties.getCrypto().isEnabled()) {
            return crypto().unpackEnvelope(rawResponse);
        }
        return rawResponse;
    }

    @Override
    public Receipt send(OutboundMessage message) {
        String path = "/gateway/" + message.getMsgType();
        String payload = message.getPayload() == null ? "" : message.getPayload();
        String response;
        try {
            response = restClient.post()
                    .uri(path)
                    .contentType(JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(sendBody(payload))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            // 平台连不上不抛断业务链路：落一条失败回执，由台账体现，便于重试（与本地 mock 失败语义一致）
            log.error("[医保通道] 发送失败 msgType={} tradeNo={}：{}", message.getMsgType(), message.getTradeNo(), e.getMessage());
            return new Receipt(false, null, null, "医保平台连接失败：" + e.getMessage());
        }
        try {
            String business = receiveBody(response);
            JsonNode data = objectMapper.readTree(business).path("data");
            int infcode = data.path("infcode").asInt(-1);
            String receiptNo = text(data, "receiptNo");
            String reply = objectMapper.writeValueAsString(data);
            if (infcode == 0) {
                return new Receipt(true, receiptNo, reply, null);
            }
            String errMsg = text(data, "errMsg");
            return new Receipt(false, receiptNo, reply,
                    TextUtil.hasText(errMsg) ? errMsg : "医保平台返回失败（infcode=" + infcode + "）");
        } catch (IllegalStateException e) {
            log.error("[医保通道] 回执安全校验失败 msgType={} tradeNo={}：{}",
                    message.getMsgType(), message.getTradeNo(), e.getMessage());
            return new Receipt(false, null, response, "医保平台回执安全校验失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[医保通道] 回执解析失败 msgType={} tradeNo={} body={}",
                    message.getMsgType(), message.getTradeNo(), response, e);
            return new Receipt(false, null, response, "医保平台回执解析失败：" + e.getMessage());
        }
    }

    @Override
    public List<RemoteSettlement> queryDayBill(LocalDate billDate) {
        String response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/gateway/dayBill")
                            .queryParam("billDate", billDate.toString())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            // 对账必须让使用方感知平台不可用，不能静默返回空（空会被误判成“本地多出差笔”）
            log.error("[医保通道] 日对账拉取失败 billDate={}：{}", billDate, e.getMessage());
            throw new BusinessException("医保平台对账接口不可用：" + e.getMessage());
        }
        try {
            JsonNode data = objectMapper.readTree(response).path("data");
            if (!data.isArray()) {
                return List.of();
            }
            List<RemoteSettlement> list = new ArrayList<>(data.size());
            for (JsonNode it : data) {
                list.add(new RemoteSettlement(
                        text(it, "tradeNo"),
                        text(it, "settlementNo"),
                        decimal(it, "totalAmount"),
                        decimal(it, "insurancePay"),
                        text(it, "receiptNo")));
            }
            return list;
        } catch (Exception e) {
            log.error("[医保通道] 对账结果解析失败 billDate={} body={}", billDate, response, e);
            throw new BusinessException("医保平台对账结果解析失败：" + e.getMessage());
        }
    }

    @Override
    public InsuredStatus checkInsured(String insuranceNo, String idCard, String name) {
        // 实名核验口径：主档有身份证 → 身份证是金标准，按身份证查参保（查无即未参保）；
        // 主档无身份证 → 退按医保卡号查参保，命中后比姓名防冒用他人卡号
        boolean byIdCard = TextUtil.hasText(idCard);
        String response;
        try {
            String body = objectMapper.writeValueAsString(byIdCard
                    ? Map.of("idCard", idCard)
                    : Map.of("insuranceNo", insuranceNo));
            response = restClient.post()
                    .uri("/insure/person/query")
                    .contentType(JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(sendBody(body))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            // 平台连不上：参保状态不可知，按「不可用」返回，结算侧据此拒医保（fail-closed，不因专线抖动放行医保）
            log.error("[医保通道] 参保查询失败 insuranceNo={} idCard={}：{}", insuranceNo, idCard, e.getMessage());
            return new InsuredStatus(false, false, null, null, "医保平台连接失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[医保通道] 参保查询请求构造失败 insuranceNo={} idCard={}：{}", insuranceNo, idCard, e.getMessage());
            return new InsuredStatus(false, false, null, null, "医保平台参保查询请求构造失败：" + e.getMessage());
        }
        try {
            String business = receiveBody(response);
            JsonNode root = objectMapper.readTree(business);
            int code = root.path("code").asInt(-1);
            if (code != 200) {
                String msg = root.path("message").asText(null);
                return new InsuredStatus(false, false, null, null,
                        TextUtil.hasText(msg) ? msg : "医保平台参保查询失败（code=" + code + "）");
            }
            JsonNode data = root.path("data");
            if (data.isMissingNode() || data.isNull()) {
                return byIdCard
                        ? new InsuredStatus(false, false, null, null,
                        "按身份证未查询到参保记录（未参保登记），请核对身份证/医保卡号或改按自费结算")
                        : new InsuredStatus(false, false, null, null,
                        "参保库查无此卡号（" + insuranceNo + "），未参保登记");
            }
            // 按卡号查询时做姓名比对，防止冒用他人医保卡号
            if (!byIdCard && TextUtil.hasText(name) && !name.equals(text(data, "name"))) {
                return new InsuredStatus(false, false, null, null,
                        "参保人身份与患者档案不符（姓名不一致），请核对医保卡号或改按自费结算");
            }
            // 按身份证查到参保人后，再比对其参保卡号与患者主档卡号：不一致 = 卡号填错/冒用他人卡号
            String insuredNo = text(data, "insuranceNo");
            if (byIdCard && TextUtil.hasText(insuredNo) && !insuredNo.equals(insuranceNo)) {
                return new InsuredStatus(false, false, null, null,
                        "医保卡号与参保人身份不符（按身份证查到的参保卡号为 " + insuredNo + "），"
                                + "可能填错或冒用他人卡号，请核对后改按自费结算");
            }
            boolean active = data.path("personStatus").asInt(0) == 1;
            return new InsuredStatus(true, active,
                    text(data, "insuranceType"), text(data, "name"),
                    active ? null : "参保状态为停保（" + (TextUtil.hasText(insuredNo) ? insuredNo : insuranceNo) + "）");
        } catch (IllegalStateException e) {
            log.error("[医保通道] 参保查询回执安全校验失败 insuranceNo={} idCard={}：{}", insuranceNo, idCard, e.getMessage());
            return new InsuredStatus(false, false, null, null, "医保平台回执安全校验失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[医保通道] 参保查询结果解析失败 insuranceNo={} idCard={} body={}", insuranceNo, idCard, response, e);
            return new InsuredStatus(false, false, null, null, "医保平台参保查询结果解析失败：" + e.getMessage());
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isTextual() ? v.asText() : null;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isNumber() ? v.decimalValue() : null;
    }

    @Override
    public SettleSplit preSettle(SettleRequest request) {
        return callSettle("/gateway/2201", request);
    }

    @Override
    public SettleSplit formalSettle(SettleRequest request) {
        return callSettle("/gateway/2301", request);
    }

    /**
     * 实时结算（2201/2301 共用）：把 SettleRequest JSON 加签加密后 POST 到 mss，解信封后解析 {@code data} 段。
     * 平台连不上/返回非 200 时，返回 infcode=-1 且带 errMsg，由调用方降级为「自费收」。
     */
    private SettleSplit callSettle(String path, SettleRequest request) {
        String body;
        try {
            body = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            SettleSplit err = new SettleSplit();
            err.setInfcode(-1);
            err.setTradeNo(request.getTradeNo());
            err.setErrMsg("医保结算请求序列化失败：" + e.getMessage());
            return err;
        }
        String response;
        try {
            log.info("[医保通道] 调用 {} 请求报文：{}", path, body);
            response = restClient.post()
                    .uri(path)
                    .contentType(JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(sendBody(body))
                    .retrieve()
                    .body(String.class);
            log.info("[医保通道] 调用 {} 响应报文：{}", path, response);
        } catch (RestClientException e) {
            boolean uncertain = isUncertainTransport(e);
            log.error("[医保通道] 实时结算调用失败 path={} tradeNo={} uncertain={}：{}",
                    path, request.getTradeNo(), uncertain, e.getMessage());
            SettleSplit err = new SettleSplit();
            err.setInfcode(-1);
            err.setTradeNo(request.getTradeNo());
            err.setUncertain(uncertain);
            err.setErrMsg(uncertain
                    ? "医保平台响应超时/连接中断（结果不确定，须先冲正确认再降级）：" + e.getMessage()
                    : "医保平台连接失败：" + e.getMessage());
            return err;
        }
        try {
            String business = receiveBody(response);
            JsonNode root = objectMapper.readTree(business);
            int code = root.path("code").asInt(-1);
            if (code != 200) {
                String msg = root.path("message").asText("医保平台返回失败");
                SettleSplit err = new SettleSplit();
                err.setInfcode(-1);
                err.setTradeNo(request.getTradeNo());
                err.setErrMsg(msg);
                return err;
            }
            JsonNode d = root.path("data");
            SettleSplit split = new SettleSplit();
            split.setInfcode(d.path("infcode").asInt(-1));
            split.setTradeNo(text(d, "tradeNo"));
            split.setReceiptNo(text(d, "receiptNo"));
            split.setInsuranceType(text(d, "insuranceType"));
            split.setName(text(d, "name"));
            split.setTotal(decimal(d, "total"));
            split.setInsurancePay(decimal(d, "insurancePay"));
            split.setPersonalAccount(decimal(d, "personalAccount"));
            split.setCashPay(decimal(d, "cashPay"));
            split.setSelfPay(decimal(d, "selfPay"));
            split.setDeductible(decimal(d, "deductible"));
            split.setAccountBalanceAfter(decimal(d, "accountBalanceAfter"));
            split.setWarnMsg(text(d, "warnMsg"));
            split.setErrMsg(text(d, "errMsg"));
            return split;
        } catch (IllegalStateException e) {
            log.error("[医保通道] 实时结算回执安全校验失败 path={} tradeNo={}：{}", path, request.getTradeNo(), e.getMessage());
            SettleSplit err = new SettleSplit();
            err.setInfcode(-1);
            err.setTradeNo(request.getTradeNo());
            err.setErrMsg("医保平台回执安全校验失败：" + e.getMessage());
            return err;
        } catch (Exception e) {
            log.error("[医保通道] 实时结算响应解析失败 path={} body={}", path, response, e);
            SettleSplit err = new SettleSplit();
            err.setInfcode(-1);
            err.setTradeNo(request.getTradeNo());
            err.setErrMsg("医保平台响应解析失败");
            return err;
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

    @Override
    public Receipt cancelRealtimeSettle(String origTradeNo, String tradeNo, String reason) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("msgType", "2305");
        payload.put("tradeNo", tradeNo);
        payload.put("origTradeNo", origTradeNo);
        payload.put("cancelReason", reason == null ? "HIS 不确定态自动冲正" : reason);
        payload.put("sendTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String response;
        try {
            response = restClient.post()
                    .uri("/gateway/2305")
                    .contentType(JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(sendBody(payload.toString()))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            log.error("[医保通道] 2305 冲正发送失败 origTradeNo={} tradeNo={}：{}", origTradeNo, tradeNo, e.getMessage());
            return new Receipt(false, null, null, "医保平台连接失败：" + e.getMessage());
        }
        try {
            String business = receiveBody(response);
            JsonNode root = objectMapper.readTree(business);
            int code = root.path("code").asInt(-1);
            JsonNode data = root.path("data");
            int infcode = data.path("infcode").asInt(-1);
            String errMsg = text(data, "errMsg");
            if (errMsg == null) {
                errMsg = root.path("message").asText(null);
            }
            if (code == 200 && infcode == 0) {
                return new Receipt(true, text(data, "receiptNo"), business, null);
            }
            return new Receipt(false, text(data, "receiptNo"), business,
                    TextUtil.hasText(errMsg) ? errMsg : "医保平台冲正失败（infcode=" + infcode + "）");
        } catch (Exception e) {
            log.error("[医保通道] 2305 冲正回执解析失败 origTradeNo={} body={}", origTradeNo, response, e);
            return new Receipt(false, null, response, "医保平台冲正回执解析失败：" + e.getMessage());
        }
    }
}
