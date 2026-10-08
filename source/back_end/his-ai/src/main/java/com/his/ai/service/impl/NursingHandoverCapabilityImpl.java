package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.NursingHandoverDTO;
import com.his.ai.dto.NursingHandoverLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.NursingHandoverCapability;
import com.his.ai.support.DeteriorationScoreRules;
import com.his.ai.vo.NursingHandoverPromptVariablesVO;
import com.his.ai.vo.WardHandoverVO;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.service.InpatientNursingService;
import com.his.patient.vo.NursingAssessmentVO;
import com.his.patient.vo.WardNursingFactsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 护理交接班摘要实现（G-13）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NursingHandoverCapabilityImpl implements NursingHandoverCapability {

    private static final String TEMPLATE_NAME = "nursing-handover";

    private static final String BIZ_TYPE = "nursing_handover";

    /**
     * 高风险评估口径：风险等级 3-高风险 4-极高风险
     */
    private static final int RISK_LEVEL_HIGH = 3;

    /**
     * 摘要上限（与提示词 ≤400 字纪律一致，超写截断）
     */
    private static final int SUMMARY_MAX = 500;

    /**
     * 事件文本上限（单条护理记录正文不再进事件，防止提示词被长文本撑爆）
     */
    private static final int EVENT_MAX = 120;

    private final AiExecutionService aiExecutionService;

    private final InpatientNursingService inpatientNursingService;

    // ---------------------------------------------------------------- 事实文本

    @Override
    public WardHandoverVO compose(NursingHandoverDTO dto) {
        LocalDate date = dto.getShiftDate() == null ? LocalDate.now() : dto.getShiftDate();
        LocalDateTime begin;
        LocalDateTime end;
        switch (dto.getShift()) {
            case 1 -> {
                begin = date.atTime(8, 0);
                end = date.atTime(16, 0);
            }
            case 2 -> {
                begin = date.atTime(16, 0);
                end = TimeUtil.dayStart(date.plusDays(1));
            }
            default -> {
                begin = TimeUtil.dayStart(date);
                end = date.atTime(8, 0);
            }
        }
        WardNursingFactsVO facts = inpatientNursingService.wardShiftFacts(
                dto.getWardId(), begin, end, dto.getShift());

        WardHandoverVO vo = new WardHandoverVO();
        vo.setWardName(facts.getWardName() == null ? ("病区" + dto.getWardId()) : facts.getWardName());
        vo.setShiftText(facts.getShiftText());
        vo.setWindowText(begin.format(DateFormats.DATETIME_MINUTE) + " ~ " + end.format(DateFormats.DATETIME_MINUTE));
        vo.setInHospitalCount(facts.getCensus().getInHospitalCount());
        vo.setDischargeCount(facts.getCensus().getDischargeCount());
        vo.setNewAdmissions(newAdmissionLines(facts));
        vo.setAbnormalEvents(eventLines(DeteriorationScoreRules.abnormalEvents(facts.getVitalRows())));
        vo.setRiskAssessments(riskLines(facts.getAssessmentRows()));
        vo.setDegraded(false);
        vo.setDegradeReason("");

        NursingHandoverPromptVariablesVO variables = new NursingHandoverPromptVariablesVO();
        variables.setWardName(vo.getWardName());
        variables.setShiftText(vo.getShiftText());
        variables.setWindowText(vo.getWindowText());
        variables.setFactsText(factsText(vo, facts));

        Optional<NursingHandoverLlmOutputDTO> output = aiExecutionService.call(
                AiCallDTO.builder()
                        .capabilityKey(AiCapabilityKeys.NURSING_HANDOVER)
                        .templateName(TEMPLATE_NAME)
                        .variables(variables)
                        .bizType(BIZ_TYPE)
                        // 病区级调用没有单一 bizId；用病区ID + 班次窗起点做检索锚点
                        .inputDigest("ward=" + dto.getWardId() + "; window=" + begin.format(DateFormats.DATETIME_MINUTE))
                        .build(),
                NursingHandoverLlmOutputDTO.class);
        if (output.isEmpty() || !TextUtil.hasText(output.get().getSummary())) {
            vo.setSource(2);
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.NURSING_HANDOVER));
            vo.setSummary(ruleSummary(vo));
            return vo;
        }
        vo.setSource(1);
        vo.setSummary(TextUtil.cut(output.get().getSummary(), SUMMARY_MAX, ""));
        return vo;
    }

    private List<String> newAdmissionLines(WardNursingFactsVO facts) {
        List<String> lines = new ArrayList<>();
        for (WardNursingFactsVO.AdmissionBrief a : facts.getCensus().getNewAdmissions()) {
            lines.add((a.getBedNo() == null ? "" : a.getBedNo() + "床 ")
                    + (a.getPatientName() == null ? "" : a.getPatientName() + " ")
                    + (TextUtil.hasText(a.getAdmitDiagnosisName()) ? "（" + a.getAdmitDiagnosisName() + "）" : ""));
        }
        return lines;
    }

    private List<String> eventLines(List<String> events) {
        List<String> lines = new ArrayList<>();
        for (String e : events) {
            lines.add(TextUtil.cut(e, EVENT_MAX, ""));
        }
        return lines;
    }

    private List<String> riskLines(List<NursingAssessmentVO> rows) {
        List<String> lines = new ArrayList<>();
        for (NursingAssessmentVO r : rows) {
            if (r.getRiskLevel() == null || r.getRiskLevel() < RISK_LEVEL_HIGH) {
                continue;
            }
            lines.add((r.getBedNo() == null ? "" : r.getBedNo() + "床 ")
                    + (r.getPatientName() == null ? "" : r.getPatientName() + " ")
                    + (r.getAssessTypeText() == null ? "" : r.getAssessTypeText() + " ")
                    + "总分" + (r.getTotalScore() == null ? "—" : r.getTotalScore())
                    + "，" + (r.getRiskLevelText() == null ? "" : r.getRiskLevelText()));
        }
        return lines;
    }

    private String factsText(WardHandoverVO vo, WardNursingFactsVO facts) {
        StringBuilder sb = new StringBuilder();
        sb.append("- 在院 ").append(vo.getInHospitalCount()).append(" 人，本班新入 ")
                .append(vo.getNewAdmissions().size()).append(" 人，出院 ").append(vo.getDischargeCount()).append(" 人\n");
        sb.append("- 新入院：").append(vo.getNewAdmissions().isEmpty() ? "无" : String.join("；", vo.getNewAdmissions())).append("\n");
        sb.append("- 体征越阈事件（").append(vo.getAbnormalEvents().size()).append(" 条）：\n");
        if (vo.getAbnormalEvents().isEmpty()) {
            sb.append("  本班次无\n");
        } else {
            for (String e : vo.getAbnormalEvents()) {
                sb.append("  - ").append(e).append("\n");
            }
        }
        sb.append("- 高/极高风险评估（").append(vo.getRiskAssessments().size()).append(" 条）：")
                .append(vo.getRiskAssessments().isEmpty() ? "本班次无" : String.join("；", vo.getRiskAssessments()));
        return sb.toString();
    }

    /**
     * 降级摘要：同一份事实按固定句式拼，读者拿到的是事实清单而不是空白
     */
    private String ruleSummary(WardHandoverVO vo) {
        StringBuilder sb = new StringBuilder();
        sb.append("【现状】").append(vo.getWardName()).append(" ").append(vo.getShiftText())
                .append("（").append(vo.getWindowText()).append("）：在院 ")
                .append(vo.getInHospitalCount()).append(" 人，新入 ")
                .append(vo.getNewAdmissions().size()).append(" 人，出院 ")
                .append(vo.getDischargeCount()).append(" 人。");
        if (!vo.getNewAdmissions().isEmpty()) {
            sb.append("新入院：").append(String.join("；", vo.getNewAdmissions())).append("。");
        }
        if (vo.getAbnormalEvents().isEmpty()) {
            sb.append("本班次体征无越阈事件。");
        } else {
            sb.append("【关注】体征越阈 ").append(vo.getAbnormalEvents().size()).append(" 条：");
            for (String e : vo.getAbnormalEvents()) {
                sb.append(e).append("；");
            }
        }
        if (!vo.getRiskAssessments().isEmpty()) {
            sb.append("高/极高风险评估：").append(String.join("；", vo.getRiskAssessments())).append("。");
        }
        sb.append("（本摘要由规则模板拼接，请结合交班本核对）");
        return sb.toString();
    }
}
