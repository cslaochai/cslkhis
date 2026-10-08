package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.Icd10LlmOutputDTO;
import com.his.ai.dto.Icd10PredictDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.Icd10Capability;
import com.his.ai.service.Icd10RecallService;
import com.his.ai.vo.Icd10PredictItemVO;
import com.his.ai.vo.Icd10PredictResultVO;
import com.his.ai.vo.Icd10PromptVariablesVO;
import com.his.common.util.TextUtil;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.system.entity.SysIcd10;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ICD-10 智能编码能力（P0-1）。
 * <p>
 * <b>这不是「让 AI 猜诊断」</b>，而是「让 AI 在标准码表里做选择」。
 * 差别是整个方案成立与否的关键：
 * <ul>
 *   <li>编码范围由召回层封闭，模型编不出码表外的编码</li>
 *   <li>编码名称、分类一律以字典表为准，模型说的名称直接丢弃</li>
 *   <li>降级后有确定性的关键词回落，不会给医生一个空白界面</li>
 * </ul>
 * 医生的职责不变 —— 推荐只是把选择范围从 3 万条缩到 5 条，最终点确认的仍然是人。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Icd10CapabilityImpl implements Icd10Capability {

    private static final String TEMPLATE_NAME = "icd10-predict";

    private static final String BIZ_TYPE = "medical_record";

    /**
     * 返回条数上限，与提示词里约定的「最多 5 条」保持一致
     */
    private static final int MAX_RESULT = 5;

    private static final int MIN_CONFIDENCE = 50;

    private static final int MAX_CONFIDENCE = 100;

    private static final int REASONING_MAX_LENGTH = 80;

    private static final int OUTPUT_TOKEN_LIMIT = 1024;

    /**
     * 规则回落时的置信度分档。刻意把上限压在 80 ——
     * 规则匹配的可靠性明显低于模型，不能让它装出「高置信」。
     */
    private static final int RULE_CONFIDENCE_EXACT = 80;

    private static final int RULE_CONFIDENCE_STRONG = 70;

    private static final int RULE_CONFIDENCE_WEAK = 60;

    private static final int RULE_CONFIDENCE_MIN = 52;

    private final Icd10RecallService icd10RecallService;

    private final AiExecutionService aiExecutionService;

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    private static int ruleConfidence(int score) {
        if (score >= 100) {
            return RULE_CONFIDENCE_EXACT;
        }
        if (score >= 40) {
            return RULE_CONFIDENCE_STRONG;
        }
        if (score >= 15) {
            return RULE_CONFIDENCE_WEAK;
        }
        return RULE_CONFIDENCE_MIN;
    }

    private static String formatCandidates(List<Icd10RecallService.RecallHit> hits) {
        StringBuilder builder = new StringBuilder();
        for (Icd10RecallService.RecallHit hit : hits) {
            SysIcd10 code = hit.code();
            builder.append(code.getIcdCode()).append(' ').append(code.getIcdName());
            if (TextUtil.hasText(code.getIcdCategory())) {
                builder.append('（').append(code.getIcdCategory()).append('）');
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    private static Map<String, Integer> scoreIndex(List<Icd10RecallService.RecallHit> hits) {
        Map<String, Integer> index = new HashMap<>();
        for (Icd10RecallService.RecallHit hit : hits) {
            index.put(hit.code().getIcdCode(), hit.score());
        }
        return index;
    }

    private static int clampConfidence(Integer confidence) {
        if (confidence == null) {
            return MIN_CONFIDENCE;
        }
        return Math.max(MIN_CONFIDENCE, Math.min(MAX_CONFIDENCE, confidence));
    }

    /**
     * 推荐 ICD-10 编码
     */
    public Icd10PredictResultVO predict(Icd10PredictDTO dto) {
        long start = System.currentTimeMillis();
        Icd10PredictResultVO vo = new Icd10PredictResultVO();

        NoteText note = resolveNoteText(dto);

        List<Icd10RecallService.RecallHit> hits = icd10RecallService.recall(note.merged(), dto.getTopN());
        vo.setCandidateCount(hits.size());

        if (hits.isEmpty()) {
            vo.setDegraded(true);
            vo.setDegradeReason("ICD-10 码表无可用编码，请先维护 sys_icd10 字典");
            vo.setLatencyMs(System.currentTimeMillis() - start);
            return vo;
        }

        Map<String, SysIcd10> candidateIndex = Icd10RecallService.indexByCode(hits);
        Map<String, Integer> scoreIndex = scoreIndex(hits);

        Optional<Icd10LlmOutputDTO> llmOutput = callModel(dto, note, hits);

        if (llmOutput.isPresent()) {
            vo.setPredictions(toItems(llmOutput.get(), candidateIndex));
        }

        if (vo.getPredictions().isEmpty()) {
            // 两种情况归一处理：模型压根没返回，或返回的编码全被候选集校验拦下。
            // 后者是幻觉被拦住的正常表现，不是系统故障，但仍然要告诉医生「这是规则给的」。
            vo.setDegraded(true);
            vo.setDegradeReason(llmOutput.isPresent()
                    ? "模型推荐的编码均不在候选集内，已按关键词规则回落"
                    : aiExecutionService.degradeReasonOf(AiCapabilityKeys.ICD10));
            vo.setPredictions(ruleFallback(hits, scoreIndex));
        }

        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    /**
     * 组装病历文本。入参里给了字段就用入参（医生可能刚改过还没保存），
     * 缺的字段回落到数据库里的病历。
     */
    private NoteText resolveNoteText(Icd10PredictDTO dto) {
        String chiefComplaint = dto.getChiefComplaint();
        String presentIllness = dto.getPresentIllness();
        String specialistExam = dto.getSpecialistExam();
        String diagnosis = dto.getDiagnosis();

        boolean missingAny = !TextUtil.hasText(chiefComplaint) || !TextUtil.hasText(presentIllness)
                || !TextUtil.hasText(specialistExam) || !TextUtil.hasText(diagnosis);
        if (dto.getRecordId() != null && missingAny) {
            BizMedicalRecord record = bizMedicalRecordMapper.selectById(dto.getRecordId());
            if (record != null) {
                chiefComplaint = TextUtil.blankToDefault(chiefComplaint, record.getChiefComplaint());
                presentIllness = TextUtil.blankToDefault(presentIllness, record.getPresentIllness());
                specialistExam = TextUtil.blankToDefault(specialistExam, record.getSpecialistExam());
                diagnosis = TextUtil.blankToDefault(diagnosis, record.getDiagnosis());
            } else {
                log.warn("[AI-ICD] 病历 {} 不存在，仅使用入参文本", dto.getRecordId());
            }
        }
        return new NoteText(chiefComplaint, presentIllness, specialistExam, diagnosis);
    }

    private Optional<Icd10LlmOutputDTO> callModel(Icd10PredictDTO dto, NoteText note,
                                                  List<Icd10RecallService.RecallHit> hits) {
        Icd10PromptVariablesVO variables = new Icd10PromptVariablesVO();
        variables.setCandidateCount(String.valueOf(hits.size()));
        variables.setCandidates(formatCandidates(hits));
        variables.setChiefComplaint(TextUtil.blankToDefault(note.chiefComplaint(), "（未填写）"));
        variables.setPresentIllness(TextUtil.blankToDefault(note.presentIllness(), "（未填写）"));
        variables.setSpecialistExam(TextUtil.blankToDefault(note.specialistExam(), "（未填写）"));
        variables.setDiagnosis(TextUtil.blankToDefault(note.diagnosis(), "（未填写）"));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.ICD10)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(dto.getRecordId())
                // 病历全文只用于生成摘要，不落库明文；执行器还会再过一道正则脱敏
                .inputDigest(note.merged())
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, Icd10LlmOutputDTO.class);
    }

    /**
     * 候选集校验 + 名称归真。
     * <p>
     * 这是整个能力最关键的一段代码：模型返回的每一条都必须能在候选集里找到，
     * 找不到就丢弃；找到也<b>不使用模型给的名称</b>，一律换成字典表里的名称。
     */
    private List<Icd10PredictItemVO> toItems(Icd10LlmOutputDTO output, Map<String, SysIcd10> candidateIndex) {
        List<Icd10PredictItemVO> items = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        List<Icd10LlmOutputDTO.Prediction> predictions = output.getPredictions() == null
                ? List.<Icd10LlmOutputDTO.Prediction>of() : output.getPredictions();
        for (Icd10LlmOutputDTO.Prediction prediction : predictions) {
            String code = TextUtil.trimToNull(prediction.getIcdCode());
            if (code == null) {
                continue;
            }
            SysIcd10 dict = candidateIndex.get(code);
            if (dict == null) {
                log.warn("[AI-ICD] 模型推荐的编码 {} 不在候选集内，已丢弃（疑似幻觉）", code);
                continue;
            }
            if (!seen.add(code)) {
                continue;
            }

            Icd10PredictItemVO item = new Icd10PredictItemVO();
            item.setIcdCode(code);
            item.setIcdName(dict.getIcdName());
            item.setIcdCategory(dict.getIcdCategory());
            item.setConfidence(clampConfidence(prediction.getConfidence()));
            item.setReasoning(TextUtil.ellipsis(prediction.getReasoning(), REASONING_MAX_LENGTH));
            item.setSource("LLM");
            items.add(item);
        }

        items.sort(Comparator.comparingInt(Icd10PredictItemVO::getConfidence).reversed());
        return items.size() > MAX_RESULT ? new ArrayList<>(items.subList(0, MAX_RESULT)) : items;
    }

    /**
     * 规则回落：直接用召回得分给出候选。
     * <p>
     * 注意这里<b>不会</b>输出「理由」——规则没有推理能力，编一段理由比不给更糟。
     */
    private List<Icd10PredictItemVO> ruleFallback(List<Icd10RecallService.RecallHit> hits,
                                                  Map<String, Integer> scoreIndex) {
        List<Icd10PredictItemVO> items = new ArrayList<>();
        for (Icd10RecallService.RecallHit hit : hits) {
            if (items.size() >= MAX_RESULT) {
                break;
            }
            int score = scoreIndex.getOrDefault(hit.code().getIcdCode(), 0);
            if (score <= 0) {
                continue;
            }
            Icd10PredictItemVO item = new Icd10PredictItemVO();
            item.setIcdCode(hit.code().getIcdCode());
            item.setIcdName(hit.code().getIcdName());
            item.setIcdCategory(hit.code().getIcdCategory());
            item.setConfidence(ruleConfidence(score));
            item.setReasoning("病历文本与编码名称存在字面重合");
            item.setSource("RULE");
            items.add(item);
        }
        return items;
    }

    /**
     * 病历文本（送入模型与召回层的字段集合）
     */
    private record NoteText(String chiefComplaint, String presentIllness,
                            String specialistExam, String diagnosis) {

        private static void appendIfPresent(StringBuilder builder, String text) {
            if (TextUtil.hasText(text)) {
                builder.append(text).append(' ');
            }
        }

        /**
         * 拼接为单段文本，供召回层做字面匹配
         */
        String merged() {
            StringBuilder builder = new StringBuilder();
            appendIfPresent(builder, chiefComplaint);
            appendIfPresent(builder, presentIllness);
            appendIfPresent(builder, specialistExam);
            appendIfPresent(builder, diagnosis);
            return builder.toString();
        }
    }
}
