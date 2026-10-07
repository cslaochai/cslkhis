package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.DeteriorationLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.DeteriorationAlertCapability;
import com.his.ai.support.DeteriorationScoreRules;
import com.his.ai.vo.DeteriorationAlertPromptVariablesVO;
import com.his.ai.vo.DeteriorationExplainVO;
import com.his.ai.vo.DeteriorationScanVO;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.patient.service.InpatientNursingService;
import com.his.patient.vo.NursingVitalFactVO;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 危重预警实现（G-12）。
 * <p><b>评分是事实层</b>（{@link DeteriorationScoreRules} 全代码）；wardScan 纯代码无模型调用、
 * 不出审计行；explain 只在 alertLevel ≥1 时才调模型要观察建议（纪律 2：模型只解释事实），
 * 模型不可用时评分照常返回、{@code degraded=true}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeteriorationAlertCapabilityImpl implements DeteriorationAlertCapability {
    private static final String TEMPLATE_NAME = "deterioration-alert";
    private static final String BIZ_TYPE = "deterioration_alert";
    /**
     * 评分窗口：取最近 24h 的最新体征（更早的体征不代表当前状态）
     */
    private static final int LOOKBACK_HOURS = 24;
    /**
     * 建议上限（与提示词 ≤150 字纪律一致）
     */
    private static final int ADVICE_MAX = 200;
    private final AiExecutionService aiExecutionService;
    private final InpatientNursingService inpatientNursingService;
    private final DictCacheService dictCacheService;

    @Override
    public List<DeteriorationScanVO> wardScan(Long wardId) {
        List<NursingVitalFactVO> vitals =
                inpatientNursingService.latestVitalsByWard(wardId, LocalDateTime.now().minusHours(LOOKBACK_HOURS));
        List<DeteriorationScanVO> result = new ArrayList<>(vitals.size());
        for (NursingVitalFactVO vital : vitals) {
            DeteriorationScanVO row = new DeteriorationScanVO();
            row.setAdmissionId(vital.getAdmissionId());
            row.setPatientName(vital.getPatientName());
            row.setBedNo(vital.getBedNo());
            row.setMeasureTime(vital.getMeasureTime() == null ? null : vital.getMeasureTime().format(DateFormats.DATETIME));
            DeteriorationScoreRules.DeteriorationScore score = DeteriorationScoreRules.score(vital);
            row.setItems(DeteriorationScoreRules.items(vital));
            row.setTotalScore(score.getTotalScore());
            int level = DeteriorationScoreRules.alertLevel(score.getTotalScore());
            row.setAlertLevel(level);
            row.setAlertText(dictCacheService.getDicDataLabel("biz_ai_deteriorationAlertLevelEnum", level));
            result.add(row);
        }
        // 预警级降序、同级按分数降序 —— 值班护士从最差的看起
        result.sort((a, b) -> {
            int byLevel = Integer.compare(b.getAlertLevel(), a.getAlertLevel());
            return byLevel != 0 ? byLevel : Integer.compare(b.getTotalScore(), a.getTotalScore());
        });
        return result;
    }

    // ---------------------------------------------------------------- 模型层

    @Override
    public DeteriorationExplainVO explain(Long admissionId) {
        NursingVitalFactVO vital =
                inpatientNursingService.latestVitalByAdmission(admissionId, LocalDateTime.now().minusHours(LOOKBACK_HOURS));

        DeteriorationExplainVO vo = new DeteriorationExplainVO();
        vo.setAdmissionId(admissionId);
        vo.setDegraded(false);
        vo.setDegradeReason("");
        vo.setTriggeredFacts(new ArrayList<>());
        if (vital == null) {
            vo.setTotalScore(DeteriorationScoreRules.NO_DATA_SCORE);
            vo.setAlertLevel(0);
            vo.setAlertText("近24小时无体征数据，无法评分");
            return vo;
        }
        vo.setPatientName(vital.getPatientName());
        vo.setBedNo(vital.getBedNo());
        vo.setWardName(vital.getWardName());
        vo.setMeasureTime(vital.getMeasureTime() == null ? null : vital.getMeasureTime().format(DateFormats.DATETIME));
        DeteriorationScoreRules.DeteriorationScore score = DeteriorationScoreRules.score(vital);
        vo.setItems(DeteriorationScoreRules.items(vital));
        vo.setTotalScore(score.getTotalScore());
        int level = DeteriorationScoreRules.alertLevel(score.getTotalScore());
        vo.setAlertLevel(level);
        vo.setAlertText(dictCacheService.getDicDataLabel("biz_ai_deteriorationAlertLevelEnum", level));
        vo.setTriggeredFacts(triggeredFacts(vital));
        if (level == 0) {
            // 未达预警阈值不调模型：模型只服务预警情形（纪律 2 / 纪律 9 的反向裁剪）
            vo.setAdvice(null);
            return vo;
        }

        Optional<DeteriorationLlmOutputDTO> output = callModel(vital, score, level);
        if (output.isEmpty() || !StringUtils.hasText(output.get().getAdvice())) {
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.DETERIORATION_ALERT));
            return vo;
        }
        vo.setAdvice(TextUtil.cut(output.get().getAdvice(), ADVICE_MAX, ""));
        return vo;
    }

    // ---------------------------------------------------------------- 事实文本

    private Optional<DeteriorationLlmOutputDTO> callModel(NursingVitalFactVO vital,
                                                          DeteriorationScoreRules.DeteriorationScore score,
                                                          int level) {
        DeteriorationAlertPromptVariablesVO variables = new DeteriorationAlertPromptVariablesVO();
        variables.setPatientTag(patientTag(vital));
        variables.setMeasureTime(measureTimeText(vital));
        variables.setVitalText(vitalText(vital));
        variables.setTotalScore(String.valueOf(score.getTotalScore()));
        variables.setAlertLevel(String.valueOf(level));
        variables.setAlertText(dictCacheService.getDicDataLabel("biz_ai_deteriorationAlertLevelEnum", level));
        variables.setItemsText(itemsText(vital));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.DETERIORATION_ALERT)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(vital.getAdmissionId())
                // 输入摘要不含患者标识文本（姓名在 mask 正则之外），用 admissionId + 测量时间做检索锚点
                .inputDigest("admission=" + vital.getAdmissionId()
                        + (vital.getMeasureTime() == null ? "" : "; " + vital.getMeasureTime().format(DateFormats.DATETIME)))
                .build();
        return aiExecutionService.call(call, DeteriorationLlmOutputDTO.class);
    }

    private List<String> triggeredFacts(NursingVitalFactVO vital) {
        List<String> facts = new ArrayList<>();
        for (DeteriorationScoreRules.ScoreItem item : DeteriorationScoreRules.items(vital)) {
            if (item.score() > 0) {
                facts.add(item.name() + " " + item.valueText() + "（+" + item.score() + " 分）");
            }
        }
        return facts;
    }

    private String vitalText(NursingVitalFactVO vital) {
        StringBuilder sb = new StringBuilder();
        append(sb, "收缩压", vital.getSystolicPressure() == null ? null : vital.getSystolicPressure() + "mmHg");
        append(sb, "舒张压", vital.getDiastolicPressure() == null ? null : vital.getDiastolicPressure() + "mmHg");
        append(sb, "脉搏", vital.getPulse() == null ? null : vital.getPulse() + "次/分");
        append(sb, "呼吸", vital.getRespiration() == null ? null : vital.getRespiration() + "次/分");
        append(sb, "体温", vital.getTemperature() == null ? null : vital.getTemperature() + "℃");
        append(sb, "血氧", vital.getSpo2() == null ? null : vital.getSpo2() + "%");
        return sb.length() == 0 ? "无" : sb.toString();
    }

    private String itemsText(NursingVitalFactVO vital) {
        List<DeteriorationScoreRules.ScoreItem> items = DeteriorationScoreRules.items(vital);
        if (items.isEmpty()) {
            return "无测量数据";
        }
        StringBuilder sb = new StringBuilder();
        for (DeteriorationScoreRules.ScoreItem item : items) {
            sb.append("- ").append(item.name()).append(" ").append(item.valueText())
                    .append("：").append(item.score()).append(" 分\n");
        }
        return sb.toString();
    }

    private String patientTag(NursingVitalFactVO vital) {
        String bed = vital.getBedNo() == null ? "" : vital.getBedNo() + "床";
        String name = vital.getPatientName() == null ? "" : vital.getPatientName();
        return (bed + " " + name).trim();
    }

    private String measureTimeText(NursingVitalFactVO vital) {
        return vital.getMeasureTime() == null ? "未知" : vital.getMeasureTime().format(DateFormats.DATETIME);
    }

    private void append(StringBuilder sb, String label, String value) {
        if (value == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("；");
        }
        sb.append(label).append(" ").append(value);
    }
}