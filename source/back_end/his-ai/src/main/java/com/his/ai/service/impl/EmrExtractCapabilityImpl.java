package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.EmrExtractDTO;
import com.his.ai.dto.EmrExtractLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.EmrExtractCapability;
import com.his.ai.support.AiMaskUtils;
import com.his.ai.support.EmrFieldCatalog;
import com.his.ai.support.EmrTextTagSplitter;
import com.his.ai.vo.EmrExtractFieldVO;
import com.his.ai.vo.EmrExtractPromptVariablesVO;
import com.his.ai.vo.EmrExtractResultVO;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 病历文本结构化抽取（P1-3）。
 * <p>
 * 把一段自由文本拆进病历的各个字段。文本的三个来源：医生手打、从外院系统/上级医院病历粘贴、
 * 以及后续语音转写的输出 —— <b>这也是语音输入电子病历能成立的前提</b>：
 * 口述转成文字之后，得有东西把流水账接住并落到字段上，否则转写只是多了一段没人用的文本。
 * <p>
 * <b>三层防幻觉，缺一不可</b>（这是本能力能不能用的关键，改之前先把这三条读一遍）：
 * <ol>
 *   <li><b>字段白名单</b>：模型只能用 {@link EmrFieldCatalog} 里列出的 key。
 *       模型返回自定义字段名（「月经史」「遗传病史」）一律丢弃 ——
 *       前端不知道怎么渲染，库里也没有对应的列，和白名单外的 ICD 编码一样必须挡掉。</li>
 *   <li><b>原文依据校验（本能力的核心）</b>：每条结果必须给出 evidence，代码把 evidence
 *       做空白与标点归一后回原文做<b>子串比对</b>，查不到就整条丢弃并计入 rejectedCount。
 *       没有这一层，模型会非常自然地"顺"出「无发热」「否认药物过敏史」这类阴性描述 ——
 *       而病历上凭空多一句阴性描述，等于伪造了医生的问诊记录，比字段空着危险得多。</li>
 *   <li><b>规则优先</b>：原文本身就是主诉：… 这种带标签格式时，按标签逐字切分的
 *       结果<b>优于</b>模型的改写版本。病历是法律文书，逐字原文永远比转述可信。</li>
 *   <li><b>诊疗决策不接受模型生成</b>：{@code diagnosis} / {@code treatmentPlan} 这两个字段
 *       是医生的判断与法律文书，模型给它们的产出整条丢弃（见
 *       {@link EmrFieldCatalog#isLlmWritable}）—— 原文带标签时仍可由硬规则层逐字搬运。</li>
 *   <li><b>数值幻觉校验</b>：值里出现的每一段数字都必须在依据里找得到。
 *       文字上的增补还能靠医生过目发现，数值上的增补（「高血压」→「高血压10年」）
 *       读起来完全通顺，而病历里的每个数字都是临床事实，必须单独查。</li>
 * </ol>
 * <p>
 * <b>本能力绝不做的事</b>：不写回病历、不落库、不自动采纳。
 * 产出只是一组候选值，医生在前端逐字段点「填入」后才进表单 ——
 * 与病历内涵质控"只提示、不修改"的口径一致。
 * <p>
 * <b>体征（体温/脉搏/呼吸/血压）不交给模型</b>，走正则 + 取值范围校验：数值一旦被模型
 * "顺"一下就是一条错误记录。抽出来的体征同样只是候选值。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrExtractCapabilityImpl implements EmrExtractCapability {

    private static final String TEMPLATE_NAME = "emr-extract";

    private static final String BIZ_TYPE = "medical_record";

    private static final String SOURCE_HARD_RULE = "HARD_RULE";

    private static final String SOURCE_LLM = "LLM";

    /**
     * 输入上限。超长直接截断并在结果里标记 —— 医生贴了一整份出院小结进来时，
     * 至少要让"后半段没抽"这件事是可见的，而不是悄悄丢掉。
     */
    private static final int MAX_INPUT_LENGTH = 4000;

    private static final int MAX_LLM_FIELDS = 12;

    private static final int MAX_VALUE_LENGTH = 500;

    private static final int MAX_EVIDENCE_LENGTH = 200;

    private static final int MAX_REJECTED_NOTES = 5;

    /**
     * 依据片段归一后的最短长度。太短的依据（一两个字）即使碰巧命中原文也说明不了什么
     */
    private static final int MIN_EVIDENCE_LENGTH = 3;

    private static final int DIGEST_LENGTH = 300;

    private static final int OUTPUT_TOKEN_LIMIT = 2048;

    /**
     * 值里的一段数字（含小数），用于数值幻觉校验
     */
    private static final Pattern NUMBER_RUN = Pattern.compile("\\d+(?:\\.\\d+)?");

    /**
     * 中文数字 → 阿拉伯数字的逐字符映射表（下标即数字）
     */
    private static final char[] CN_DIGITS = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    private final AiExecutionService aiExecutionService;

    /**
     * 模型输出的净化：白名单 + 原文依据校验。
     * <p>
     * 被丢弃的条目会记进 {@code notes} —— 医生需要知道"系统丢了东西"，
     * 而不是以为原文里就只有这些内容。
     */
    private static LlmIndex indexLlmFields(Optional<EmrExtractLlmOutputDTO> output, String promptText) {
        Map<String, EmrExtractLlmOutputDTO.Field> result = new LinkedHashMap<>();
        List<String> notes = new ArrayList<>();
        if (output.isEmpty() || output.get().getFields() == null) {
            return new LlmIndex(result, 0, notes);
        }
        String normalizedText = normalizeForMatch(promptText);
        int rejected = 0;
        for (EmrExtractLlmOutputDTO.Field field : output.get().getFields()) {
            if (result.size() >= MAX_LLM_FIELDS) {
                rejected++;
                reject(notes, String.format("结果超过 %d 条上限，多余条目已丢弃", MAX_LLM_FIELDS));
                continue;
            }
            String key = field.getField() == null ? "" : field.getField().trim();

            if (!EmrFieldCatalog.isTextKey(key)) {
                rejected++;
                reject(notes, String.format("字段「%s」不在可写白名单内", TextUtil.cut(key, 20, "（空）")));
                continue;
            }
            String label = EmrFieldCatalog.getText(key);
            // 诊疗决策字段不接受模型产出（诊断权在医生；处理意见是法律文书）
            if (!EmrFieldCatalog.isLlmWritable(key)) {
                rejected++;
                reject(notes, String.format("「%s」属于诊疗决策，不接受模型生成（原文带标签时才搬运）", label));
                continue;
            }
            if (!TextUtil.hasText(field.getValue())) {
                rejected++;
                reject(notes, String.format("「%s」的值为空", label));
                continue;
            }
            if (result.containsKey(key)) {
                rejected++;
                continue;
            }
            // 核心防线：依据必须能在原文里原样找到
            String evidence = normalizeForMatch(field.getEvidence());
            if (evidence.length() < MIN_EVIDENCE_LENGTH) {
                rejected++;
                reject(notes, String.format("「%s」没给出可核对的原文依据", label));
                continue;
            }
            if (!normalizedText.contains(evidence)) {
                rejected++;
                reject(notes, String.format("「%s」的依据在原文中查不到，已丢弃", label));
                continue;
            }
            // 依据真实 ≠ 值真实：值里的数字还必须能从依据里找到
            if (hasFabricatedNumber(field.getValue(), field.getEvidence())) {
                rejected++;
                reject(notes, String.format("「%s」的值里出现了依据中查不到的数字，已丢弃", label));
                continue;
            }
            result.put(key, field);
        }
        return new LlmIndex(result, rejected, notes);
    }

    /**
     * 体征提取：正则 + 取值范围双保险。取值范围是最后一道网 ——
     * 「呼吸」的单字母缩写 R 很容易撞上 CRP、RBC 这类检验缩写，
     * 正则的 lookaround 挡不住时，5~60 这个区间能再挡一层。
     */
    private static Map<String, String> extractVitals(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        for (EmrFieldCatalog.VitalField field : EmrFieldCatalog.vitalFields()) {
            Matcher matcher = field.pattern().matcher(text);
            if (!matcher.find()) {
                continue;
            }
            String value = matcher.group(1);
            try {
                double number = Double.parseDouble(value);
                if (number >= field.min() && number <= field.max()) {
                    result.put(field.key(), value);
                }
            } catch (NumberFormatException ignored) {
                // 正则保证是数字，这里只是防御
            }
        }
        String[] bloodPressure = EmrFieldCatalog.extractBloodPressure(text);
        if (bloodPressure != null) {
            result.put("systolicPressure", bloodPressure[0]);
            result.put("diastolicPressure", bloodPressure[1]);
        }
        return result;
    }

    private static void merge(EmrExtractResultVO vo,
                              Map<String, String> byRule,
                              Map<String, EmrExtractLlmOutputDTO.Field> byLlm,
                              Map<String, String> vitals) {
        for (EmrFieldCatalog.TextField field : EmrFieldCatalog.textFields()) {
            String ruleValue = byRule.get(field.key());
            if (TextUtil.hasText(ruleValue)) {
                // 规则命中即胜出：逐字原文优先于模型转述
                vo.getFields().add(build(field.key(), field.label(),
                        TextUtil.cut(ruleValue, MAX_VALUE_LENGTH, ""),
                        SOURCE_HARD_RULE,
                        TextUtil.cut(ruleValue, MAX_EVIDENCE_LENGTH, "")));
                continue;
            }
            EmrExtractLlmOutputDTO.Field llmField = byLlm.get(field.key());
            if (llmField != null) {
                vo.getFields().add(build(field.key(), field.label(),
                        TextUtil.cut(llmField.getValue(), MAX_VALUE_LENGTH, ""),
                        SOURCE_LLM,
                        TextUtil.cut(llmField.getEvidence(), MAX_EVIDENCE_LENGTH, "")));
            }
        }

        for (EmrFieldCatalog.VitalField field : EmrFieldCatalog.vitalFields()) {
            String value = vitals.get(field.key());
            if (TextUtil.hasText(value)) {
                vo.getFields().add(build(field.key(), field.label(), value, SOURCE_HARD_RULE, value));
            }
        }
        addVital(vo, vitals, "systolicPressure");
        addVital(vo, vitals, "diastolicPressure");
    }

    // 硬规则层：体征

    private static void addVital(EmrExtractResultVO vo, Map<String, String> vitals, String key) {
        String value = vitals.get(key);
        if (TextUtil.hasText(value)) {
            vo.getFields().add(build(key, EmrFieldCatalog.getText(key), value, SOURCE_HARD_RULE, value));
        }
    }

    private static EmrExtractFieldVO build(String key, String label, String value,
                                           String source, String evidence) {
        EmrExtractFieldVO vo = new EmrExtractFieldVO();
        vo.setField(key);
        vo.setFieldLabel(label);
        vo.setValue(value);
        vo.setSource(source);
        vo.setEvidence(evidence);
        return vo;
    }

    /**
     * 依据比对用的归一化：去掉空白与标点、忽略大小写、把中文数字统一成阿拉伯数字。
     * <p>
     * 刻意宽松 —— 模型复述原文时常常改标点、加空格，卡太死会把正确结果也丢掉；
     * 归一化之后仍然是「连续子串」比对，编造的内容过不了。
     * <p>
     * <b>中文数字必须一起归一</b>：原文写「三天前开始咳嗽」，模型引原文时很容易写成
     * 「3天前开始咳嗽」—— 这是同一句话，不该被当成"依据查不到"而丢掉。
     * 逐字符映射（零→0…九→9）对原文和依据<b>用同一张表</b>，
     * 所以不会破坏「子串」这个关系，只是把两边拉齐。
     */
    static String normalizeForMatch(String text) {
        if (!TextUtil.hasText(text)) {
            return "";
        }
        String value = text.replaceAll("[\\s\\u3000]", "")
                .replaceAll("[，,。.；;：:、！!？?（）()\\[\\]【】\"'“”‘’`~·]", "")
                .toLowerCase();
        for (int digit = 0; digit < CN_DIGITS.length; digit++) {
            value = value.replace(CN_DIGITS[digit], (char) ('0' + digit));
        }
        return value;
    }

    private static void reject(List<String> notes, String note) {
        if (notes.size() < MAX_REJECTED_NOTES) {
            notes.add(note);
        }
    }

    /**
     * 数值幻觉校验：值里出现的每一段数字，都必须能在依据里找到。
     * <p>
     * 为什么单独查数字：模型改写文字时很容易"顺手"补一个看起来合理的数量 ——
     * 原文写「高血压」它写成「高血压10年」，原文写「咳嗽」它写成「咳嗽3周」。
     * 文字上的增补还能被子串比对和医生过目拦住，数值上的增补最不容易被发现：
     * 它读起来完全通顺，而病历里的每一个数字都是临床事实。
     * <p>
     * 只拦<b>凭空多出来的</b>数字。依据里出现过的数字照常通过，
     * 中文写法的数字（「三年」）也一起归一后再比，不会误伤。
     */
    private static boolean hasFabricatedNumber(String value, String evidence) {
        if (!TextUtil.hasText(value)) {
            return false;
        }
        String normalizedEvidence = normalizeForMatch(evidence);
        Matcher matcher = NUMBER_RUN.matcher(normalizeForMatch(value));
        while (matcher.find()) {
            if (!normalizedEvidence.contains(matcher.group())) {
                return true;
            }
        }
        return false;
    }

    public EmrExtractResultVO execute(EmrExtractDTO dto) {
        long start = System.currentTimeMillis();

        String rawText = dto.getRawText() == null ? "" : dto.getRawText().trim();
        if (!TextUtil.hasText(rawText)) {
            throw new BusinessException("待抽取的文本不能为空");
        }

        EmrExtractResultVO vo = new EmrExtractResultVO();
        if (rawText.length() > MAX_INPUT_LENGTH) {
            rawText = rawText.substring(0, MAX_INPUT_LENGTH);
            vo.setTruncated(true);
        }

        // 硬规则一：按字段标签逐字切分。用**原文**而非脱敏文本 ——
        // 切出来的内容最终可能被医生采纳写进病历，逐字保留原文才有意义。
        Map<String, String> byRule = EmrTextTagSplitter.split(rawText);
        // 硬规则二：体征
        Map<String, String> vitals = extractVitals(rawText);

        // 模型看到的是脱敏后的文本（病历自由文本里常夹着身份证号、手机号）
        String promptText = AiMaskUtils.mask(rawText);
        Optional<EmrExtractLlmOutputDTO> llmOutput = callModel(dto, promptText);

        List<String> rejectedNotes = new ArrayList<>();
        LlmIndex llmIndex = indexLlmFields(llmOutput, promptText);
        Map<String, EmrExtractLlmOutputDTO.Field> byLlm = llmIndex.fields();
        rejectedNotes.addAll(llmIndex.notes());

        merge(vo, byRule, byLlm, vitals);

        vo.setFieldCount(vo.getFields().size());
        vo.setRejectedCount(llmIndex.rejectedCount());
        vo.setRejectedNotes(rejectedNotes);

        if (llmOutput.isEmpty()) {
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.EMR_EXTRACT)
                    + "；本次仅按原文中的字段标签切分，未带标签的内容不会被抽取");
        }

        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    private Optional<EmrExtractLlmOutputDTO> callModel(EmrExtractDTO dto, String promptText) {
        Integer gender = dto.getGender();
        Integer age = dto.getAge();
        if (dto.getRecordId() != null) {
            BizMedicalRecord record = bizMedicalRecordMapper.selectById(dto.getRecordId());
            if (record != null) {
                gender = record.getGender();
                age = record.getAge();
            }
        }

        EmrExtractPromptVariablesVO variables = new EmrExtractPromptVariablesVO();
        variables.setGender(SysGenderEnum.getText(gender));
        variables.setAge(age == null ? "（未填写）" : age + "岁");
        variables.setFieldCatalog(EmrFieldCatalog.writableFieldPrompt());
        variables.setRawText(promptText);

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.EMR_EXTRACT)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(dto.getRecordId())
                .inputDigest(AiMaskUtils.digest(promptText, DIGEST_LENGTH))
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, EmrExtractLlmOutputDTO.class);
    }

    /**
     * 模型输出的净化结果。
     * <p>
     * {@code rejectedCount} 与 {@code notes} 必须分开：notes 有 5 条上限（只是样本），
     * 计数则是全量 —— 否则「丢了 12 条」会被显示成「丢了 5 条」。
     */
    private record LlmIndex(Map<String, EmrExtractLlmOutputDTO.Field> fields,
                            int rejectedCount,
                            List<String> notes) {
    }
}
