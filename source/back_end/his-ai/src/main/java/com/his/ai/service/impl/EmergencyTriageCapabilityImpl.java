package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.EmergencyTriageDTO;
import com.his.ai.dto.EmergencyTriageLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.EmergencyTriageCapability;
import com.his.ai.vo.EmergencyTriagePromptVariablesVO;
import com.his.ai.vo.EmergencyTriageResultVO;
import com.his.appoint.entity.BizEmergency;
import com.his.appoint.mapper.BizEmergencyMapper;
import com.his.appoint.support.EmergencyTriageRules;
import com.his.appoint.support.EmergencyTriageRules.RedFlag;
import com.his.appoint.support.EmergencyTriageRules.VitalSigns;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 急诊智能分诊建议能力（P1-2）—— <b>本模块风险等级最高的一项</b>。
 * <p>
 * 分诊错误直接危及生命，因此这里的约束比其它能力严格得多：
 * <ol>
 *   <li><b>只升不降</b>。最终建议级别取「硬规则级别、模型建议、人工已选级别」三者中最严重的一个。
 *       系统永远不会把护士选的分级降下来 —— 最坏情况是提示「建议升级」，
 *       而护士可以不理它。这个方向的不对称是刻意的：建议升级最多浪费一次评估，
 *       建议降级可能让危重患者排在后面。</li>
 *   <li><b>生命体征红旗由代码判定</b>（{@link EmergencyTriageRules}），不经模型。
 *       SpO2 &lt; 90%、GCS ≤ 8 这类事实不需要「理解」。</li>
 *   <li><b>不写库</b>。{@code triage_level / zone / green_channel} 永远由护士确认后写入。
 *       本能力只返回建议值与依据。</li>
 *   <li><b>缺生命体征必须明示</b>（{@code vitalSignsMissing}）。实测库里 {@code vital_signs}
 *       为 null —— 只看主诉做分诊的可信度远低于完整输入，UI 上必须让人看出来，
 *       而不是让一串建议显得同样权威。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyTriageCapabilityImpl implements EmergencyTriageCapability {

    private static final String TEMPLATE_NAME = "emergency-triage";

    private static final String BIZ_TYPE = "emergency";

    private static final int MAX_ACTIONS = 6;

    private static final int MAX_RED_FLAGS = 8;

    private static final int OUTPUT_TOKEN_LIMIT = 1536;

    private static final String BASIS_HARD_RULE = "HARD_RULE";

    private static final String BASIS_LLM = "LLM";

    private static final String BASIS_MANUAL = "MANUAL";

    private static final String BASIS_NONE = "NONE";

    private static final String CHANNEL_NONE = "无";

    private final BizEmergencyMapper bizEmergencyMapper;

    private final AiExecutionService aiExecutionService;

    /**
     * 级别合并：取最严重（数值最小）的一个。
     * <p>
     * 这是「只升不降」的唯一实现点。任何一个非空来源只要比人工已选更严重，
     * 建议值就会更严重；没有任何来源比人工已选更严重时，建议值就等于人工已选值，
     * 也就是「不建议改动」。
     */
    private static Integer minLevel(Integer... levels) {
        Integer result = null;
        for (Integer level : levels) {
            if (level == null) {
                continue;
            }
            result = result == null ? level : Math.min(result, level);
        }
        return result;
    }

    private static String resolveBasis(Integer hardLevel, Integer llmLevel,
                                       Integer currentLevel, Integer suggested) {
        if (suggested == null) {
            return BASIS_NONE;
        }
        // 硬规则优先标源：同样的级别由确定性和概率同时给出时，说成「规则判定」更准确
        if (hardLevel != null && suggested.equals(hardLevel)) {
            return BASIS_HARD_RULE;
        }
        if (llmLevel != null && suggested.equals(llmLevel)) {
            return BASIS_LLM;
        }
        if (currentLevel != null && suggested.equals(currentLevel)) {
            return BASIS_MANUAL;
        }
        return BASIS_NONE;
    }

    /**
     * 绿色通道合并：只认非「无」的值，因此永远不会把已确定的通道降回「无」。
     */
    private static String pickChannel(String keywordChannel, String llmChannel, String currentChannel) {
        if (StringUtils.hasText(keywordChannel) && !CHANNEL_NONE.equals(keywordChannel)) {
            return keywordChannel;
        }
        if (StringUtils.hasText(llmChannel) && !CHANNEL_NONE.equals(llmChannel)) {
            return llmChannel;
        }
        if (StringUtils.hasText(currentChannel) && !CHANNEL_NONE.equals(currentChannel)) {
            return currentChannel;
        }
        return CHANNEL_NONE;
    }

    private static Integer clampLevel(Integer level) {
        if (level == null) {
            return null;
        }
        return Math.max(EmergencyTriageRules.LEVEL_CRITICAL,
                Math.min(EmergencyTriageRules.LEVEL_NON_URGENT, level));
    }

    private static String buildRuleReasoning(EmergencyTriageResultVO vo, List<RedFlag> flags) {
        if (flags.isEmpty()) {
            return String.format("未命中生命体征红旗征象，主诉关键词未识别到绿色通道。当前按人工分级 %s 处置，系统不提出变更建议。",
                    vo.getCurrentLevelText());
        }
        StringBuilder builder = new StringBuilder("命中确定性红旗征象：");
        builder.append(String.join("；", flags.stream().map(RedFlag::label).toList()));
        builder.append("。系统建议级别不低于 ").append(vo.getHardLevelText()).append('。');
        return builder.toString();
    }

    private static String buildTip(EmergencyTriageResultVO vo) {
        if (Boolean.TRUE.equals(vo.getUpgradeRecommended())) {
            return String.format("系统建议将分诊级别由「%s」升级为「%s」，请结合患者实际情况确认。"
                            + "本建议不会自动修改你已选择的分级。",
                    vo.getCurrentLevelText(), vo.getSuggestedLevelText());
        }
        if (vo.getCurrentLevel() == null) {
            return String.format("该系统建议级别为「%s」，请确认后写入分诊级别。本建议不落库。",
                    vo.getSuggestedLevelText());
        }
        return String.format("系统未发现需要变更分级的依据，维持「%s」。本建议不落库。",
                vo.getCurrentLevelText());
    }

    private static List<String> nonBlank(List<String> values) {
        if (values == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                result.add(TextUtil.cut(value.trim(), 120));
            }
        }
        return result;
    }

    private static List<String> concat(List<String> first, List<String> second) {
        List<String> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
    }

    private static List<String> dedupe(List<String> values) {
        Set<String> unique = new LinkedHashSet<>(values);
        return new ArrayList<>(unique);
    }

    private static List<String> limit(List<String> values, int max) {
        if (values == null || values.size() <= max) {
            return values == null ? new ArrayList<>() : new ArrayList<>(values);
        }
        return new ArrayList<>(values.subList(0, max));
    }

    public EmergencyTriageResultVO suggest(EmergencyTriageDTO dto) {
        long start = System.currentTimeMillis();

        BizEmergency entity = dto.getEmergencyId() == null
                ? null : bizEmergencyMapper.selectById(dto.getEmergencyId());
        if (dto.getEmergencyId() != null && entity == null) {
            throw new BusinessException("急诊记录不存在：" + dto.getEmergencyId());
        }

        String chiefComplaint = StringUtils.hasText(dto.getChiefComplaint())
                ? dto.getChiefComplaint() : (entity == null ? null : entity.getChiefComplaint());
        String rawVitalSigns = StringUtils.hasText(dto.getVitalSigns())
                ? dto.getVitalSigns() : (entity == null ? null : entity.getVitalSigns());
        if (!StringUtils.hasText(chiefComplaint) && !StringUtils.hasText(rawVitalSigns)) {
            throw new BusinessException("主诉与生命体征至少需要提供一项，否则无法给出分诊建议");
        }

        VitalSigns vitals = EmergencyTriageRules.parse(rawVitalSigns);

        EmergencyTriageResultVO vo = new EmergencyTriageResultVO();
        vo.setEmergencyId(entity == null ? null : entity.getId());
        vo.setEmergencyNo(entity == null ? null : entity.getEmergencyNo());
        vo.setPatientName(entity == null ? null : entity.getPatientName());
        Integer gender = dto.getGender() != null ? dto.getGender() : (entity == null ? null : entity.getGender());
        Integer age = dto.getAge() != null ? dto.getAge() : (entity == null ? null : entity.getAge());
        vo.setGenderText(SysGenderEnum.getText(gender));
        vo.setAge(age);
        vo.setChiefComplaint(chiefComplaint);
        vo.setVitalSignsText(vitals.describe());
        vo.setVitalSignsMissing(vitals.isEmpty());

        vo.setCurrentLevel(entity == null ? null : entity.getTriageLevel());
        vo.setCurrentLevelText(EmergencyTriageRules.levelText(vo.getCurrentLevel()));
        vo.setCurrentZone(entity == null ? null : entity.getZone());
        vo.setCurrentGreenChannel(entity == null ? null : entity.getGreenChannel());

        // 硬规则层
        List<RedFlag> flags = new ArrayList<>(EmergencyTriageRules.evaluate(vitals));
        flags.addAll(EmergencyTriageRules.evaluateChiefComplaint(chiefComplaint));
        Integer hardLevel = EmergencyTriageRules.hardLevelOf(flags);
        vo.setHardLevel(hardLevel);
        vo.setHardLevelText(hardLevel == null ? "未命中红旗征象" : EmergencyTriageRules.levelText(hardLevel));
        vo.setRedFlags(limit(flags.stream().map(RedFlag::label).toList(), MAX_RED_FLAGS));

        String channelByKeyword = EmergencyTriageRules.detectGreenChannel(chiefComplaint);

        // 模型层
        boolean useModel = !Boolean.FALSE.equals(dto.getUseModel());
        Integer llmLevel = null;
        String llmChannel = null;
        String llmReasoning = null;
        List<String> llmActions = new ArrayList<>();
        List<String> llmRedFlags = new ArrayList<>();

        if (useModel) {
            Optional<EmergencyTriageLlmOutputDTO> output = callModel(vo, vitals, flags, channelByKeyword);
            if (output.isPresent()) {
                EmergencyTriageLlmOutputDTO value = output.get();
                llmLevel = clampLevel(value.getSuggestLevel());
                if (EmergencyTriageRules.isValidChannel(value.getSuggestGreenChannel())) {
                    llmChannel = value.getSuggestGreenChannel().trim();
                }
                llmReasoning = TextUtil.cut(value.getReasoning(), 400);
                llmActions = limit(nonBlank(value.getRecommendActions()), MAX_ACTIONS);
                llmRedFlags = limit(nonBlank(value.getRedFlags()), MAX_RED_FLAGS);
                // 模型报出的红旗只做补充展示，不参与级别计算 —— 级别计算必须可复现
                vo.setRedFlags(dedupe(concat(vo.getRedFlags(), llmRedFlags)));
            } else {
                vo.setDegraded(true);
                vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.EMERGENCY_TRIAGE)
                        + "；本次仅返回生命体征硬规则的判定结果");
            }
        } else {
            vo.setDegraded(true);
            vo.setDegradeReason("调用方指定不调用模型，本次仅返回生命体征硬规则的判定结果");
        }

        // 合并（只升不降）
        Integer suggested = minLevel(hardLevel, llmLevel, vo.getCurrentLevel());
        vo.setSuggestedLevel(suggested);
        vo.setSuggestedLevelText(EmergencyTriageRules.levelText(suggested));
        vo.setSuggestedZone(EmergencyTriageRules.zoneOf(suggested));
        vo.setSuggestedGreenChannel(pickChannel(channelByKeyword, llmChannel, vo.getCurrentGreenChannel()));
        vo.setLevelBasis(resolveBasis(hardLevel, llmLevel, vo.getCurrentLevel(), suggested));
        vo.setUpgradeRecommended(vo.getCurrentLevel() != null && suggested != null
                && suggested < vo.getCurrentLevel());

        List<String> actions = new ArrayList<>();
        for (RedFlag flag : flags) {
            if (StringUtils.hasText(flag.action())) {
                actions.add(flag.action());
            }
        }
        actions.addAll(EmergencyTriageRules.channelActions(vo.getSuggestedGreenChannel()));
        actions.addAll(llmActions);
        vo.setRecommendActions(limit(dedupe(actions), MAX_ACTIONS));

        vo.setReasoning(StringUtils.hasText(llmReasoning) ? llmReasoning : buildRuleReasoning(vo, flags));
        vo.setTip(buildTip(vo));

        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    private Optional<EmergencyTriageLlmOutputDTO> callModel(EmergencyTriageResultVO vo,
                                                            VitalSigns vitals,
                                                            List<RedFlag> flags,
                                                            String channelByKeyword) {
        EmergencyTriagePromptVariablesVO variables = new EmergencyTriagePromptVariablesVO();
        variables.setGender(vo.getGenderText());
        variables.setAge(vo.getAge() == null ? "（未填写）" : vo.getAge() + "岁");
        variables.setChiefComplaint(TextUtil.blankToDefault(vo.getChiefComplaint(), "（未填写）"));
        variables.setVitalSigns(vitals.describe());
        variables.setCurrentLevel(vo.getCurrentLevelText());
        variables.setHardLevel(vo.getHardLevelText());
        variables.setHardRedFlags(flags.isEmpty()
                ? "（无）"
                : String.join("\n", flags.stream().map(flag -> "- " + flag.label()).toList()));
        variables.setKeywordChannel(channelByKeyword);

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.EMERGENCY_TRIAGE)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(vo.getEmergencyId())
                .inputDigest(TextUtil.blankToDefault(vo.getChiefComplaint(), "（未填写）") + " | " + vitals.describe())
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, EmergencyTriageLlmOutputDTO.class);
    }
}
