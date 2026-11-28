package com.his.medicaltech.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.base.Result;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.medicaltech.config.SimlabProperties;
import com.his.medicaltech.dto.SimInspectionResultDTO;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.support.SimSignatureUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仿真平台结果回调入口（外部服务端主动推送，无登录态）。
 *
 * <p>仿真平台延时完成检查后回调本端点，回传检查所见 / 结论；身份由 HMAC-SHA256 签名头证明，
 * 不由登录态证明。签名口径见 {@link SimSignatureUtil}；端点已在 SecurityConfig 外部回调白名单内。
 *
 * <p>落库口径：记录置「已出结果」，报告进「待审核」队列，不替诊断医师签名定稿。
 */
@Slf4j
@Tag(name = "仿真平台回调")
@RestController
@RequestMapping("/medicaltech/sim")
@RequiredArgsConstructor
public class SimInspectionCallbackController {

    private static final int CLOCK_SKEW_SECONDS = 300;
    private static final int NONCE_PURGE_THRESHOLD = 4096;

    private final SimlabProperties props;
    private final MedicalTechService medicalTechService;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, Long> nonces = new ConcurrentHashMap<>();

    @Operation(summary = "接收仿真平台检查结果回传")
    @PostMapping("/inspection/result")
    public Result<Void> inspectionResult(HttpServletRequest request) throws IOException {
        if (!props.isEnabled() || !TextUtil.hasText(props.getSecret())) {
            log.error("仿真回调未启用或未配置 his.simlab.secret，拒绝接收 ip={}", request.getRemoteAddr());
            return Result.error(503, "仿真平台回调未启用，服务端拒绝接收");
        }

        String timestamp = request.getHeader(SimSignatureUtil.HEADER_TIMESTAMP);
        String nonce = request.getHeader(SimSignatureUtil.HEADER_NONCE);
        String signature = request.getHeader(SimSignatureUtil.HEADER_SIGNATURE);
        if (!TextUtil.hasText(timestamp) || !TextUtil.hasText(nonce) || !TextUtil.hasText(signature)) {
            log.warn("仿真回调缺少签名头 ip={}", request.getRemoteAddr());
            return Result.error(401, "缺少仿真回调签名头");
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException e) {
            return Result.error(401, "仿真回调时间戳格式无效");
        }
        long skewMs = CLOCK_SKEW_SECONDS * 1000L;
        long now = System.currentTimeMillis();
        if (Math.abs(now - ts) > skewMs) {
            log.warn("仿真回调时间戳超出偏差 ip={} ts={}", request.getRemoteAddr(), ts);
            return Result.error(401, "仿真回调时间戳超出允许偏差");
        }

        byte[] body = request.getInputStream().readAllBytes();
        String expected = SimSignatureUtil.sign(props.getSecret(), timestamp.trim(), nonce.trim(), body);
        if (!SimSignatureUtil.matches(expected, signature.trim())) {
            log.warn("仿真回调验签失败 ip={} uri={}", request.getRemoteAddr(), request.getServletPath());
            return Result.error(401, "仿真回调签名无效");
        }
        if (isReplay(nonce.trim(), now, skewMs)) {
            log.warn("仿真回调 nonce 重放 ip={} nonce={}", request.getRemoteAddr(), nonce);
            return Result.error(401, "仿真回调已被接收过（nonce 重复）");
        }

        SimInspectionResultDTO dto;
        try {
            dto = objectMapper.readValue(body, SimInspectionResultDTO.class);
        } catch (IOException e) {
            return Result.error(400, "回调报文格式不正确");
        }
        if (dto.getRecordId() == null || !TextUtil.hasText(dto.getResultDescription())) {
            return Result.error(400, "检查记录ID与检查所见不能为空");
        }

        try {
            medicalTechService.applySimInspectionResult(dto);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        }
        return Result.success("结果已接收", null);
    }

    private boolean isReplay(String nonce, long now, long ttlMs) {
        long expireAt = now + Math.max(ttlMs, 60_000L);
        if (nonces.size() > NONCE_PURGE_THRESHOLD) {
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
}
