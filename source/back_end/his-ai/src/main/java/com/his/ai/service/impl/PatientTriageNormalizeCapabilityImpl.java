package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.PatientTriageNormalizeDTO;
import com.his.ai.dto.PatientTriageNormalizeLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.PatientTriageNormalizeCapability;
import com.his.ai.support.PatientTextGuard;
import com.his.ai.vo.PatientTriageNormalizeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 患者端导诊口语归一实现。
 *
 * <p><b>模型在这里的权限只有"换一种说法"：</b>输入一句口语，输出一组标准症状词、
 * 一句检索文本、几条追问。科室推荐由 {@code biz_triage_rule} 完成，模型完全不参与 ——
 * 导诊推荐错了不会有任何报错，后果是患者挂错号白跑一趟，所以这条边界必须画死在代码里。
 *
 * <p><b>降级姿态是"用原话查"而不是"查不了"：</b>关键词匹配本来就是拿原话跑的，
 * 归一只是提高命中率的增益项。模型挂掉时患者看到的功能与今天完全一致，
 * 因此前端不需要为降级弹任何提示（报告解读那套"弹 AI 不可用只会让患者更慌"同理）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientTriageNormalizeCapabilityImpl implements PatientTriageNormalizeCapability {

    private static final String TEMPLATE_NAME = "triage-normalize";

    private static final String BIZ_TYPE = "triage";

    /** 检索文本上限：超了说明模型在写作文，截断直接用 */
    private static final int SEARCH_TEXT_MAX = 100;

    /** 症状词个数上限 */
    private static final int MAX_TERMS = 8;

    /** 单个症状词长度上限 */
    private static final int TERM_MAX_LENGTH = 12;

    /** 追问条数上限 */
    private static final int MAX_FOLLOW_UPS = 3;

    /** 单条追问长度上限 */
    private static final int FOLLOW_UP_MAX_LENGTH = 40;

    private final AiExecutionService aiExecutionService;

    private final PatientTextGuard textGuard;

    @Override
    public PatientTriageNormalizeVO execute(PatientTriageNormalizeDTO dto) {
        String raw = dto.getDescription() == null ? "" : dto.getDescription().trim();

        PatientTriageNormalizeVO vo = new PatientTriageNormalizeVO();
        vo.setSearchText(raw);
        vo.setTerms(List.of());
        vo.setFollowUps(List.of());
        vo.setSource("rule");
        vo.setDegraded(true);
        vo.setDegradeReason("");

        Optional<PatientTriageNormalizeLlmOutputDTO> output = callModel(raw);
        if (output.isEmpty()) {
            // 模型不可用：用原话查即可，功能与没有这个能力时完全一致
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.PATIENT_TRIAGE_NORMALIZE));
            return vo;
        }

        PatientTriageNormalizeLlmOutputDTO llm = output.get();
        List<String> terms = splitTerms(llm.getTerms());
        List<String> followUps = safeFollowUps(llm.getFollowUps());
        String searchText = buildSearchText(raw, llm.getSearchText(), terms);

        vo.setTerms(terms);
        vo.setFollowUps(followUps);
        vo.setSearchText(searchText);
        vo.setSource("model");
        vo.setDegraded(false);
        return vo;
    }

    // ---------------------------------------------------------------- 模型层

    private Optional<PatientTriageNormalizeLlmOutputDTO> callModel(String description) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("description", description);

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.PATIENT_TRIAGE_NORMALIZE)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .inputDigest(truncate(description, 60))
                .useLiteModel(true)
                .maxTokens(256)
                .build();

        return aiExecutionService.call(call, PatientTriageNormalizeLlmOutputDTO.class);
    }

    // ---------------------------------------------------------------- 清洗层

    /**
     * 检索文本：优先用模型给的，但<b>必须仍包含患者原话</b>。
     * <p>
     * 只拿归一后的短句去查会丢掉"三天了""右边""越来越重"这类限定信息，
     * 而规则表里keywords 是可能命中这些词的。原话 + 归一词全给，命中面最大。
     */
    private String buildSearchText(String raw, String fromModel, List<String> terms) {
        StringBuilder builder = new StringBuilder(raw);
        if (StringUtils.hasText(fromModel)) {
            String value = truncate(fromModel.trim(), SEARCH_TEXT_MAX);
            if (!raw.contains(value)) {
                builder.append('，').append(value);
            }
        }
        for (String term : terms) {
            if (!builder.toString().contains(term)) {
                builder.append('，').append(term);
            }
        }
        return truncate(builder.toString(), SEARCH_TEXT_MAX * 2);
    }

    private static List<String> splitTerms(String terms) {
        if (!StringUtils.hasText(terms)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : terms.split("[、,，;；\\s]+")) {
            String term = part.trim();
            if (!StringUtils.hasText(term) || term.length() > TERM_MAX_LENGTH) {
                continue;
            }
            if (!result.contains(term)) {
                result.add(term);
            }
            if (result.size() >= MAX_TERMS) {
                break;
            }
        }
        return result;
    }

    /**
     * 追问过闸。
     * <p>
     * 追问是"医生可能还会问你什么"，天然会提到症状，但绝不该出现诊断和用药 ——
     * 命中闸的整条丢弃（追问少一条不影响导诊，放一条"建议先吃点止痛药"出去才是事故）。
     */
    private List<String> safeFollowUps(List<String> followUps) {
        if (followUps == null || followUps.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String item : followUps) {
            if (!StringUtils.hasText(item)) {
                continue;
            }
            String guarded = textGuard.guard(truncate(item.trim(), FOLLOW_UP_MAX_LENGTH),
                    AiCapabilityKeys.PATIENT_TRIAGE_NORMALIZE);
            if (StringUtils.hasText(guarded) && !result.contains(guarded)) {
                result.add(guarded);
            }
            if (result.size() >= MAX_FOLLOW_UPS) {
                break;
            }
        }
        return result;
    }

    private static String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return text == null ? "" : text;
        }
        String value = text.trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
