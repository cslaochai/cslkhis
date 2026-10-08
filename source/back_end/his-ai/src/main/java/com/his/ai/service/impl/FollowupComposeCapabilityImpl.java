package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.FollowupComposeDTO;
import com.his.ai.dto.FollowupComposeLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.FollowupComposeCapability;
import com.his.ai.support.PatientTextGuard;
import com.his.ai.vo.FollowupComposePromptVariablesVO;
import com.his.ai.vo.FollowupComposeVO;
import com.his.common.util.TextUtil;
import com.his.emr.enums.FollowupTypeEnum;
import com.his.emr.service.ChronicRecordService;
import com.his.emr.vo.ChronicRecordListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 随访话术草拟实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowupComposeCapabilityImpl implements FollowupComposeCapability {

    private static final String TEMPLATE_NAME = "followup-compose";

    private static final String BIZ_TYPE = "followup";

    /**
     * 话术长度上限：提示词纪律是 60~150 字，超了截断
     */
    private static final int CONTENT_MAX = 200;

    /**
     * 病种上下文条数上限：慢病档案多的患者不把提示词撑爆
     */
    private static final int MAX_DISEASES = 5;

    private final AiExecutionService aiExecutionService;

    private final ChronicRecordService chronicRecordService;

    private final PatientTextGuard textGuard;

    // ---------------------------------------------------------------- 模型层

    @Override
    public FollowupComposeVO execute(FollowupComposeDTO dto) {
        String typeLabel = FollowupTypeEnum.getText(dto.getFollowupType());
        if (typeLabel == null) {
            throw new com.his.common.exception.BusinessException("随访类型不合法");
        }
        String diseaseContext = buildDiseaseContext(dto);

        FollowupComposeVO vo = new FollowupComposeVO();
        vo.setContent(buildRuleContent(typeLabel));
        vo.setSource("rule");
        vo.setDegraded(true);
        vo.setDegradeReason("");

        Optional<FollowupComposeLlmOutputDTO> output = callModel(typeLabel, diseaseContext);
        if (output.isEmpty()) {
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.FOLLOWUP_COMPOSE));
        } else {
            String guarded = textGuard.guard(TextUtil.cut(output.get().getContent(), CONTENT_MAX, ""),
                    AiCapabilityKeys.FOLLOWUP_COMPOSE);
            if (guarded == null) {
                vo.setDegradeReason("模型文案越界被患者文案硬闸拦下，已回落类型模板");
            } else {
                vo.setContent(guarded);
                vo.setSource("model");
                vo.setDegraded(false);
            }
        }
        return vo;
    }

    // ---------------------------------------------------------------- 清洗层

    private Optional<FollowupComposeLlmOutputDTO> callModel(String typeLabel, String diseaseContext) {
        FollowupComposePromptVariablesVO variables = new FollowupComposePromptVariablesVO();
        variables.setFollowupTypeName(typeLabel);
        variables.setDiseaseContext(diseaseContext);

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.FOLLOWUP_COMPOSE)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .inputDigest(TextUtil.cut(typeLabel + " " + diseaseContext, 60, ""))
                .useLiteModel(true)
                .maxTokens(256)
                .build();

        return aiExecutionService.call(call, FollowupComposeLlmOutputDTO.class);
    }

    /**
     * 病种上下文：前端带来的诊断优先，慢病档案的在管病种补齐。
     * 只给"患者得的是什么病"，不给药名和指标，模型的发挥空间到此为止。
     */
    private String buildDiseaseContext(FollowupComposeDTO dto) {
        Set<String> diseases = new LinkedHashSet<>();
        if (TextUtil.hasText(dto.getDiagnosis())) {
            diseases.add(dto.getDiagnosis().trim());
        }
        try {
            for (ChronicRecordListVO record : chronicRecordService.activeList(dto.getPatientId())) {
                if (TextUtil.hasText(record.getDiseaseName())) {
                    diseases.add(record.getDiseaseName().trim());
                }
                if (diseases.size() >= MAX_DISEASES) {
                    break;
                }
            }
        } catch (Exception e) {
            // 病种上下文只是增益项：读不到就空着，话术照常拟
            log.warn("[AI-随访话术] 病种上下文读取失败 patientId={}", dto.getPatientId(), e);
        }
        return diseases.isEmpty() ? "暂无慢病档案与诊断信息" : TextUtil.cut(String.join("、", diseases), 120, "");
    }

    /**
     * 类型模板：不带任何医学内容，只保留关怀+遵医嘱+反馈引导三段式
     */
    private String buildRuleContent(String typeLabel) {
        return "您好，这是一条" + typeLabel + "提醒：请遵医嘱按时复查、规律作息。"
                + "如方便，请在小程序「我的随访」里反馈近况；有不适请及时来院就诊。";
    }
}
