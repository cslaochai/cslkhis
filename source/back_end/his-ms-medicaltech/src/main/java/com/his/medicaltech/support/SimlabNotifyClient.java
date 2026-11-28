package com.his.medicaltech.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.common.util.TextUtil;
import com.his.medicaltech.config.SimlabProperties;
import com.his.medicaltech.entity.BizInspectionRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * HIS → 仿真平台下发客户端。
 *
 * <p>两个时点：缴费建单后推 {@code ASSIGNED}（仿真平台留存待检快照）；设备科签到后推
 * {@code ARRIVED}（仿真平台开始排队计时）。下发失败只记日志，不阻断收费 / 签到主流程 ——
 * 仿真链路挂了不能影响真实业务单据落库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimlabNotifyClient {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SimlabProperties props;
    private final ObjectMapper objectMapper;

    private volatile HttpClient httpClient;

    /** 缴费建单：把检查单快照交给仿真平台留存。 */
    public void notifyAssigned(BizInspectionRecord record) {
        post(props.getAssignPath(), "ASSIGNED", record);
    }

    /** 签到到检：通知仿真平台患者已到设备科，开始排队计时。 */
    public void notifyArrived(BizInspectionRecord record) {
        post(props.getArrivePath(), "ARRIVED", record);
    }

    private void post(String path, String event, BizInspectionRecord record) {
        if (!props.isEnabled()) {
            return;
        }
        if (!TextUtil.hasText(props.getSecret())) {
            log.warn("[simlab] 未配置 his.simlab.secret，跳过 {} 下发（recordNo={}）", event, record.getRecordNo());
            return;
        }
        try {
            String body = buildBody(event, record);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nonce = SimSignatureUtil.newNonce();
            String signature = SimSignatureUtil.sign(props.getSecret(), timestamp, nonce, bytes);

            HttpRequest request = HttpRequest.newBuilder(URI.create(props.getBaseUrl() + path))
                    .timeout(Duration.ofMillis(props.getRequestTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header(SimSignatureUtil.HEADER_TIMESTAMP, timestamp)
                    .header(SimSignatureUtil.HEADER_NONCE, nonce)
                    .header(SimSignatureUtil.HEADER_SIGNATURE, signature)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build();

            HttpResponse<String> resp = client().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                log.warn("[simlab] {} 下发 HTTP {}：recordNo={} body={}", event, resp.statusCode(),
                        record.getRecordNo(), abbreviate(resp.body()));
            }
        } catch (Exception e) {
            log.warn("[simlab] {} 下发异常：recordNo={} {}: {}", event, record.getRecordNo(),
                    e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private String buildBody(String event, BizInspectionRecord record) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("event", event);
        body.put("recordId", record.getId());
        body.put("recordNo", nullToEmpty(record.getRecordNo()));
        body.put("applyId", record.getApplyId());
        body.put("applyNo", nullToEmpty(record.getApplyNo()));
        body.put("patientId", record.getPatientId());
        body.put("patientNo", nullToEmpty(record.getPatientNo()));
        body.put("patientName", nullToEmpty(record.getPatientName()));
        body.put("gender", record.getGender());
        body.put("age", record.getAge());
        body.put("visitDate", formatDate(record.getVisitDate()));
        body.put("applyDeptName", nullToEmpty(record.getApplyDeptName()));
        body.put("applyDoctorName", nullToEmpty(record.getApplyDoctorName()));
        body.put("inspectionItemId", record.getInspectionItemId());
        body.put("inspectionItemCode", nullToEmpty(record.getInspectionItemCode()));
        body.put("inspectionItemName", nullToEmpty(record.getInspectionItemName()));
        body.put("inspectionDeptName", nullToEmpty(record.getInspectionDeptName()));
        body.put("bodyPart", nullToEmpty(record.getBodyPart()));
        body.put("clinicalDiagnosis", nullToEmpty(record.getClinicalDiagnosis()));
        body.put("checkInTime", formatDateTime(record.getCheckInTime()));
        return body.toString();
    }

    private HttpClient client() {
        HttpClient local = httpClient;
        if (local == null) {
            synchronized (this) {
                local = httpClient;
                if (local == null) {
                    local = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()))
                            .build();
                    httpClient = local;
                }
            }
        }
        return local;
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private static String formatDateTime(LocalDateTime time) {
        return time == null ? "" : DATE_TIME.format(time);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 200 ? text : text.substring(0, 200) + "...";
    }
}
