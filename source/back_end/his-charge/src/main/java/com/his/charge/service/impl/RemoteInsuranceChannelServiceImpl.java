package com.his.charge.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.charge.service.InsuranceChannelService;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * 医保前置机出口（HTTP 实现）：把 2304/2305 报文通过 HTTP 发到独立的医保结算经办平台（back_end_mss，
 * 默认 http://localhost:8090/mss），并按账期拉取平台侧“对方账”。
 *
 * <p>与本地 mock 实现二选一，由配置 {@code his.insurance.channel.mode=remote} 激活。
 * 平台对端契约见 back_end_mss 的 GatewayController（/gateway/2304、/gateway/2305、/gateway/dayBill）。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "his.insurance.channel.mode", havingValue = "remote")
public class RemoteInsuranceChannelServiceImpl implements InsuranceChannelService {

    /** 2304/2305 报文含中文（患者姓名、诊断、科室），String 报文必须显式 UTF-8，避免按默认字符集发成乱码 */
    private static final MediaType JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public RemoteInsuranceChannelServiceImpl(
            ObjectMapper objectMapper,
            @Value("${his.insurance.channel.base-url:http://localhost:8090/mss}") String baseUrl,
            @Value("${his.insurance.channel.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${his.insurance.channel.read-timeout-ms:15000}") int readTimeoutMs) {
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        log.info("[医保通道] 已启用远程医保结算经办平台 HTTP 通道：{}", baseUrl);
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
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            // 平台连不上不抛断业务链路：落一条失败回执，由台账体现，便于重试（与本地 mock 失败语义一致）
            log.error("[医保通道] 发送失败 msgType={} tradeNo={}：{}", message.getMsgType(), message.getTradeNo(), e.getMessage());
            return new Receipt(false, null, null, "医保平台连接失败：" + e.getMessage());
        }
        try {
            JsonNode data = objectMapper.readTree(response).path("data");
            int infcode = data.path("infcode").asInt(-1);
            String receiptNo = text(data, "receiptNo");
            String reply = objectMapper.writeValueAsString(data);
            if (infcode == 0) {
                return new Receipt(true, receiptNo, reply, null);
            }
            String errMsg = text(data, "errMsg");
            return new Receipt(false, receiptNo, reply,
                    TextUtil.hasText(errMsg) ? errMsg : "医保平台返回失败（infcode=" + infcode + "）");
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
                    .body(body)
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
            JsonNode root = objectMapper.readTree(response);
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
}
