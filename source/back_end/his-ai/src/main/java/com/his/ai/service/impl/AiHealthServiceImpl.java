package com.his.ai.service.impl;

import com.his.ai.config.AiConfigProvider;
import com.his.ai.config.AiProperties;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.service.AiHealthService;
import com.his.ai.service.Icd10RecallService;
import com.his.ai.support.AiDegradeGuard;
import com.his.ai.vo.AiHealthVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 能力运维服务：把「配置齐不齐、能力开没开、熔断没有」折算成一份运行时快照。
 *
 * <p><b>这里刻意不发起任何模型调用</b>：运行时状态是运维探针，每 10 秒可能打一次，
 * 每次真调一次大模型既烧钱又慢。想验证端到端连通性，直接调一次预测接口更实在。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiHealthServiceImpl implements AiHealthService {

    /**
     * 健康检查与运维看板需要覆盖的能力清单
     */
    private static final List<String> TRACKED_CAPABILITIES = List.of(
            AiCapabilityKeys.ICD10, AiCapabilityKeys.DRUG_AUDIT, AiCapabilityKeys.EMR_QC,
            AiCapabilityKeys.LAB_INTERPRET, AiCapabilityKeys.EMERGENCY_TRIAGE,
            AiCapabilityKeys.EMR_EXTRACT, AiCapabilityKeys.EMR_DRAFT,
            AiCapabilityKeys.PATIENT_REPORT_EXPLAIN, AiCapabilityKeys.PATIENT_TRIAGE_NORMALIZE,
            AiCapabilityKeys.OPERATION_QA, AiCapabilityKeys.KNOWLEDGE_QA,
            AiCapabilityKeys.PREVISIT_SUMMARY, AiCapabilityKeys.FOLLOWUP_COMPOSE,
            AiCapabilityKeys.INSURANCE_EVIDENCE,
            AiCapabilityKeys.DETERIORATION_ALERT, AiCapabilityKeys.NURSING_HANDOVER,
            AiCapabilityKeys.VOICE_TRANSCRIBE);

    private final AiConfigProvider aiConfigProvider;

    private final AiDegradeGuard degradeGuard;

    private final Icd10RecallService icd10RecallService;

    /**
     * 运行时状态快照（只读配置，不产生模型调用）
     */
    public AiHealthVO runtimeStatus() {
        AiProperties properties = aiConfigProvider.get();

        AiHealthVO vo = new AiHealthVO();
        vo.setEnabled(properties.isEnabled());
        vo.setReady(properties.isReady());
        vo.setNotReadyReason(aiConfigProvider.notReadyReason());
        vo.setProvider(properties.getProvider());
        vo.setBaseUrl(properties.getBaseUrl());
        vo.setModel(properties.getModel());
        vo.setModelLite(properties.getModelLite());
        vo.setApiKeyConfigured(StringUtils.hasText(properties.getApiKey()));
        vo.setApiKeySource(aiConfigProvider.apiKeyFromEnv() ? "env" : "yml");
        vo.setTimeoutMs(properties.getTimeoutMs());
        vo.setRetrieveTopN(properties.getRetrieveTopN());

        Map<String, Boolean> features = new LinkedHashMap<>();
        Map<String, Integer> timeouts = new LinkedHashMap<>();
        Map<String, String> models = new LinkedHashMap<>();
        Map<String, Boolean> circuitOpen = new LinkedHashMap<>();
        Map<String, Long> degradedCount = new LinkedHashMap<>();
        Map<String, String> lastFailureReason = new LinkedHashMap<>();
        for (String capability : TRACKED_CAPABILITIES) {
            // 开关状态与可用状态分开报：true 只代表「配置里允许」，
            // 是否真能用还要看上面顶层的 ready / notReadyReason。
            features.put(capability, aiConfigProvider.switchedOn(capability));
            timeouts.put(capability, aiConfigProvider.timeoutOf(capability));
            models.put(capability, aiConfigProvider.modelOf(capability, false));
            circuitOpen.put(capability, degradeGuard.isOpen(capability));
            degradedCount.put(capability, degradeGuard.degradedCountOf(capability));
            lastFailureReason.put(capability, degradeGuard.lastFailureReason(capability));
        }
        vo.setFeatures(features);
        vo.setTimeouts(timeouts);
        vo.setModels(models);
        vo.setCircuitOpen(circuitOpen);
        vo.setDegradedCount(degradedCount);
        vo.setLastFailureReason(lastFailureReason);
        return vo;
    }

    /**
     * 刷新 ICD 码表缓存。
     *
     * <p>AI 连接配置（地址/模型/超时/开关）已迁到 application.yml，启动时绑定一次，
     * 这里不刷新它们 —— 留一个「点了没反应」的刷新入口，比没有更糟。
     * ICD 码表仍是带 TTL 缓存的，维护完字典必须刷，否则最坏要等 5 分钟。
     */
    public void refreshCodeCache() {
        icd10RecallService.refresh();
        log.info("[AI] ICD 码表缓存已刷新（连接配置请改 application.yml 后重启）");
    }
}
