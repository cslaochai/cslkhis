package com.his.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.PrevisitSummaryDTO;
import com.his.ai.dto.PrevisitSummaryLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.PrevisitSummaryCapability;
import com.his.ai.vo.PrevisitSummaryVO;
import com.his.common.exception.BusinessException;
import com.his.emr.enums.PrevisitSummarySourceEnum;
import com.his.emr.service.PrevisitRecordService;
import com.his.emr.vo.PrevisitDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 预问诊病史摘要实现。
 *
 * <p><b>模型只做"把患者说过的话串成一段短文"：</b>摘要的读者是接诊医生，
 * 患者当面就诊时病历就交到医生手里，模型多写一句"考虑…"都会污染病历起点，
 * 提示词里已禁止结论性表述。摘要写回预问诊记录后医生站只读，
 * 模型挂掉时按主症状+问答明细拼规则模板，医生看到的内容等价，只是行文朴素。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrevisitSummaryCapabilityImpl implements PrevisitSummaryCapability {

    private static final String TEMPLATE_NAME = "previsit-summary";

    private static final String BIZ_TYPE = "previsit";

    /**
     * 摘要落库上限（与列表提示词的 ≤200 字纪律一致）
     */
    private static final int SUMMARY_MAX = 200;

    /**
     * 问答明细拼入提示词的上限：超了按顺序丢尾段
     */
    private static final int ANSWERS_TEXT_MAX = 800;

    private final AiExecutionService aiExecutionService;

    private final PrevisitRecordService previsitRecordService;

    private final ObjectMapper objectMapper;

    private static String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String value = text.trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    // ---------------------------------------------------------------- 模型层

    @Override
    public PrevisitSummaryVO execute(PrevisitSummaryDTO dto) {
        PrevisitDetailVO record = previsitRecordService.getByRegist(dto.getRegistId());
        if (record == null) {
            throw new BusinessException("预问诊记录不存在，请先完成病史采集");
        }

        String answersText = buildAnswersText(record.getAnswersJson());
        PrevisitSummaryVO vo = new PrevisitSummaryVO();
        vo.setSummary(buildRuleSummary(record.getMainSymptom(), answersText, record.getFreeText()));
        vo.setSource("rule");
        vo.setDegraded(true);
        vo.setDegradeReason("");

        Optional<PrevisitSummaryLlmOutputDTO> output = callModel(
                record.getMainSymptom(), answersText, record.getFreeText());
        if (output.isEmpty()) {
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.PREVISIT_SUMMARY));
        } else {
            String summary = truncate(output.get().getSummary(), SUMMARY_MAX);
            if (StringUtils.hasText(summary)) {
                vo.setSummary(summary);
                vo.setSource("model");
                vo.setDegraded(false);
            }
        }

        // 两个来源都落库：医生站报告卡直接读记录，不在这里二次生成
        previsitRecordService.saveSummary(dto.getRegistId(), vo.getSummary(),
                "model".equals(vo.getSource())
                        ? PrevisitSummarySourceEnum.MODEL.getCode()
                        : PrevisitSummarySourceEnum.RULE.getCode());
        return vo;
    }

    // ---------------------------------------------------------------- 清洗层

    private Optional<PrevisitSummaryLlmOutputDTO> callModel(String mainSymptom, String answersText, String freeText) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("mainSymptom", StringUtils.hasText(mainSymptom) ? mainSymptom : "未填写");
        variables.put("answersText", StringUtils.hasText(answersText) ? answersText : "无");
        variables.put("freeText", StringUtils.hasText(freeText) ? freeText : "无");

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.PREVISIT_SUMMARY)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .inputDigest(truncate(mainSymptom, 60))
                .useLiteModel(true)
                .maxTokens(256)
                .build();

        return aiExecutionService.call(call, PrevisitSummaryLlmOutputDTO.class);
    }

    /**
     * 问答明细是提交时的 JSON 快照，按「label：value」拼成一行行事实。
     * 解析失败不拦流程：主症状和补充描述还能兜住规则摘要。
     */
    private String buildAnswersText(String answersJson) {
        if (!StringUtils.hasText(answersJson)) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(answersJson);
            if (root.isArray()) {
                for (JsonNode item : root) {
                    String label = item.path("label").asText("");
                    String value = item.path("value").asText("");
                    if (StringUtils.hasText(label) && StringUtils.hasText(value)) {
                        parts.add(label + "：" + value.trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[AI-预问诊摘要] 问答明细解析失败，跳过该段 json={}", answersJson, e);
            return "";
        }
        return truncate(String.join("；", parts), ANSWERS_TEXT_MAX);
    }

    /**
     * 规则模板：不加任何模型措辞，只把患者自述按顺序摆好
     */
    private String buildRuleSummary(String mainSymptom, String answersText, String freeText) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(mainSymptom)) {
            builder.append("患者主诉：").append(mainSymptom.trim()).append("。");
        }
        if (StringUtils.hasText(answersText)) {
            builder.append("问诊要点：").append(answersText).append("。");
        }
        if (StringUtils.hasText(freeText)) {
            builder.append("患者补充：").append(freeText.trim()).append("。");
        }
        return truncate(builder.toString(), SUMMARY_MAX);
    }
}
