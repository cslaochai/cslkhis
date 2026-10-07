package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.InsuranceEvidenceDTO;
import com.his.ai.dto.InsuranceEvidenceLlmOutputDTO;
import com.his.ai.enums.EvidenceVerdictEnum;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.InsuranceEvidenceCapability;
import com.his.ai.vo.InsuranceEvidenceJudgmentVO;
import com.his.ai.vo.InsuranceEvidencePromptVariablesVO;
import com.his.ai.vo.InsuranceEvidenceVO;
import com.his.charge.service.ComplianceAuditService;
import com.his.charge.vo.ComplianceAuditDetailVO;
import com.his.charge.vo.ComplianceAuditItemVO;
import com.his.charge.vo.ComplianceEvidenceNarrativeVO;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 医保审核证据判定实现。
 *
 * <p><b>规则结论是事实层，模型只叠加一层「证据与怀疑的关系」</b>：
 * 规则命中（如疑似低标准入院）可能是真问题也可能是规则盲区，模型读病历证据逐条判
 * supported/refuted/insufficient，产物只提示人工复核——不写库、不改规则结论、
 * 不下处罚结论。模型挂掉时 degraded=true，规则的判定依据与整改建议照常可见，
 * 缺的只是这一层增益，功能不缺位。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceEvidenceCapabilityImpl implements InsuranceEvidenceCapability {

    private static final String TEMPLATE_NAME = "insurance-evidence";

    private static final String BIZ_TYPE = "insurance_audit";

    /**
     * 理由上限（与提示词的 ≤80 字纪律一致，模型超写就截）
     */
    private static final int REASON_MAX = 100;

    /**
     * 原文引用上限（提示词 ≤60 字）
     */
    private static final int QUOTE_MAX = 80;

    /**
     * 总评上限（提示词 ≤120 字）
     */
    private static final int OVERALL_MAX = 160;

    /**
     * 输出 token 上限：与 lab_interpret 等推理模型能力对齐，1024 会偶发被思考过程烧光导致 content 为空
     */
    private static final int OUTPUT_TOKEN_LIMIT = 2048;

    private final AiExecutionService aiExecutionService;

    private final ComplianceAuditService complianceAuditService;

    // ---------------------------------------------------------------- 模型层

    @Override
    public InsuranceEvidenceVO execute(InsuranceEvidenceDTO dto) {
        ComplianceAuditDetailVO detail = complianceAuditService.getAuditDetail(dto.getAuditId());
        List<ComplianceAuditItemVO> hits = hitItems(detail);
        if (hits.isEmpty()) {
            throw new BusinessException("该审核没有命中的风险规则，无需证据判定");
        }
        ComplianceEvidenceNarrativeVO narrative = complianceAuditService.getAiEvidenceNarrative(dto.getAuditId());

        InsuranceEvidenceVO vo = new InsuranceEvidenceVO();
        vo.setAuditId(detail.getId());
        vo.setSettlementId(detail.getSettlementId());
        vo.setSettlementNo(detail.getSettlementNo());
        vo.setDegraded(true);
        vo.setDegradeReason("");

        Optional<InsuranceEvidenceLlmOutputDTO> output = callModel(hits, narrative, detail.getAuditNo());
        if (output.isEmpty()) {
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.INSURANCE_EVIDENCE));
            return vo;
        }
        List<InsuranceEvidenceJudgmentVO> judgments = mapJudgments(output.get(), hits);
        if (judgments.isEmpty()) {
            // 模型回了但与命中规则对不上 = 产出不可用，按降级口径如实说，不伪造判定
            vo.setDegradeReason("模型返回与命中规则无法对应，已放弃本次判定，请人工核对证据");
            return vo;
        }
        vo.setJudgments(judgments);
        vo.setOverall(TextUtil.cut(output.get().getOverall(), OVERALL_MAX, ""));
        vo.setDegraded(false);
        return vo;
    }

    private Optional<InsuranceEvidenceLlmOutputDTO> callModel(List<ComplianceAuditItemVO> hits,
                                                              ComplianceEvidenceNarrativeVO narrative,
                                                              String auditNo) {
        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.INSURANCE_EVIDENCE)
                .templateName(TEMPLATE_NAME)
                .variables(buildVariables(hits, narrative))
                .bizType(BIZ_TYPE)
                .inputDigest(TextUtil.cut(auditNo, 60, ""))
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();
        return aiExecutionService.call(call, InsuranceEvidenceLlmOutputDTO.class);
    }

    // ---------------------------------------------------------------- 清洗层

    private InsuranceEvidencePromptVariablesVO buildVariables(List<ComplianceAuditItemVO> hits,
                                                              ComplianceEvidenceNarrativeVO narrative) {
        StringBuilder hitText = new StringBuilder();
        for (ComplianceAuditItemVO item : hits) {
            hitText.append("- 规则码 ").append(item.getRuleCode())
                    .append("｜").append(item.getRuleName())
                    .append("｜规则判定依据：")
                    .append(StringUtils.hasText(item.getEvidence()) ? item.getEvidence() : "无")
                    .append("\n");
        }
        InsuranceEvidencePromptVariablesVO variables = new InsuranceEvidencePromptVariablesVO();
        variables.setSettlementNo(StringUtils.hasText(narrative.getSettlementNo())
                ? narrative.getSettlementNo() : "未知");
        variables.setDrgCode(StringUtils.hasText(narrative.getDrgCode())
                ? narrative.getDrgCode() : "未分组");
        variables.setPatientTag(StringUtils.hasText(narrative.getPatientTag())
                ? narrative.getPatientTag() : "未知");
        variables.setDiagnosisText(StringUtils.hasText(narrative.getDiagnosisText())
                ? narrative.getDiagnosisText() : "未填写");
        variables.setHitItemsText(hitText.toString());
        variables.setRecordNarrative(StringUtils.hasText(narrative.getRecordNarrative())
                ? narrative.getRecordNarrative() : "无病历文本");
        variables.setOrderNames(StringUtils.hasText(narrative.getOrderNames())
                ? narrative.getOrderNames() : "无");
        variables.setLabSummary(StringUtils.hasText(narrative.getLabSummary())
                ? narrative.getLabSummary() : "无");
        variables.setMissingText(narrative.getMissingList() == null || narrative.getMissingList().isEmpty()
                ? "无" : String.join("；", narrative.getMissingList()));
        return variables;
    }

    private List<ComplianceAuditItemVO> hitItems(ComplianceAuditDetailVO detail) {
        if (detail.getItems() == null) {
            return new ArrayList<>();
        }
        return detail.getItems().stream()
                .filter(i -> i.getResult() != null && i.getResult() == 1)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 按规则码对齐：模型编造的规则码、超出枚举的 verdict 一律丢弃，判定对象不许被模型发明
     */
    private List<InsuranceEvidenceJudgmentVO> mapJudgments(InsuranceEvidenceLlmOutputDTO output,
                                                           List<ComplianceAuditItemVO> hits) {
        Map<String, ComplianceAuditItemVO> byCode = new HashMap<>();
        for (ComplianceAuditItemVO hit : hits) {
            if (StringUtils.hasText(hit.getRuleCode())) {
                byCode.putIfAbsent(hit.getRuleCode(), hit);
            }
        }
        List<InsuranceEvidenceJudgmentVO> judgments = new ArrayList<>();
        if (output.getJudgments() == null) {
            return judgments;
        }
        for (InsuranceEvidenceLlmOutputDTO.LlmJudgment j : output.getJudgments()) {
            ComplianceAuditItemVO hit = byCode.get(StringUtils.hasText(j.getRuleCode())
                    ? j.getRuleCode().trim() : "");
            if (hit == null) {
                continue;
            }
            EvidenceVerdictEnum verdict = EvidenceVerdictEnum.fromName(j.getVerdict());
            if (verdict == null) {
                continue;
            }
            InsuranceEvidenceJudgmentVO jv = new InsuranceEvidenceJudgmentVO();
            jv.setRuleCode(hit.getRuleCode());
            jv.setRuleName(hit.getRuleName());
            jv.setVerdict(verdict.getCode());
            jv.setVerdictText(verdict.getLabel());
            jv.setReason(TextUtil.cut(j.getReason(), REASON_MAX, ""));
            jv.setQuote(TextUtil.cut(j.getQuote(), QUOTE_MAX, ""));
            judgments.add(jv);
        }
        return judgments;
    }
}
