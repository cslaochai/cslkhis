package com.his.ai.service.impl;

import com.his.ai.service.LabInterpretCapability;
import com.his.ai.service.AiExecutionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.LabInterpretExecuteDTO;
import com.his.ai.dto.LabInterpretLlmOutputDTO;
import com.his.ai.vo.LabInterpretResultVO;
import com.his.ai.vo.LabItemOverviewVO;
import com.his.ai.vo.LabTrendVO;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.support.LabCriticalValueRules;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.support.LabAbnormalJudge;
import com.his.medicaltech.support.LabReferenceRangeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 检验结果解读能力（P1-1c）。
 * <p>
 * <b>分工原则：能算的一律不算给模型。</b>
 * <ul>
 *   <li><b>代码算</b>：哪些项异常、是否达到危急值、历史趋势是升是降、变化幅度 ——
 *       这些都是确定性计算，模型做反而会错（它比较两个数的大小也会出错）</li>
 *   <li><b>模型做</b>：这些异常<b>组合起来</b>意味着什么、需要进一步查什么
 *       —— 这是规则写不完的长尾</li>
 * </ul>
 * 之所以坚持这个分工，是因为它同时买到了两个好处：<b>降级可用</b>
 * （模型挂了仍然能返回异常项清单和趋势）与<b>可验证</b>
 * （只要检查规则层输出，就知道模型有没有真的多干活）。
 * <p>
 * <b>本能力默认不写库</b>。结论只是草稿，写回检验记录必须显式开启，
 * 因为那两个字段会进检验报告、进病历。见 {@link LabInterpretExecuteDTO#getOverwriteConclusion()}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabInterpretCapabilityImpl implements LabInterpretCapability {

    private static final String TEMPLATE_NAME = "lab-interpret";

    private static final String BIZ_TYPE = "laboratory_record";

    /**
     * 趋势最多回溯的历史记录数
     */
    private static final int TREND_HISTORY_LIMIT = 5;

    /**
     * 参与趋势计算的最少结果点数：只有一个点谈不上趋势
     */
    private static final int TREND_MIN_POINTS = 2;

    /**
     * 提示词里单次最多列出的结果条数，防止长面板把 token 撑爆
     */
    private static final int MAX_ITEMS_IN_PROMPT = 40;

    /**
     * 模型最多返回的关注项
     */
    private static final int MAX_ATTENTION_ITEMS = 6;

    private static final int MAX_SUGGESTIONS = 5;

    private static final int OUTPUT_TOKEN_LIMIT = 2048;

    private static final int CONCLUSION_MAX_LENGTH = 500;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BizLaboratoryRecordMapper laboratoryRecordMapper;

    private final BizLabResultMapper labResultMapper;

    private final AiExecutionService aiExecutionService;

    private static String directionOf(List<LabTrendVO.Point> points, double first, double last) {
        if (Math.abs(last - first) < 1e-9) {
            return "持平";
        }
        boolean monotonicUp = true;
        boolean monotonicDown = true;
        for (int i = 1; i < points.size(); i++) {
            double previous = points.get(i - 1).getNumericValue();
            double value = points.get(i).getNumericValue();
            if (value < previous - 1e-9) {
                monotonicUp = false;
            }
            if (value > previous + 1e-9) {
                monotonicDown = false;
            }
        }
        if (monotonicUp) {
            return "升高";
        }
        if (monotonicDown) {
            return "降低";
        }
        return "波动";
    }

    private static String magnitudeText(double first, double last) {
        if (Math.abs(first) < 1e-9) {
            return "";
        }
        BigDecimal rate = BigDecimal.valueOf(Math.abs(last - first))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(Math.abs(first)), 0, RoundingMode.HALF_UP);
        return "较首次变化 " + rate.toPlainString() + "%";
    }

    private static String formatNumber(Double value) {
        if (value == null) {
            return "";
        }
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }

    private static String buildRuleConclusion(LabInterpretResultVO vo) {
        // 先算「未判定」尾巴并预留长度：截断是从尾部砍的，
        // 不留位就会把唯一说明「这些项没有结论」的那句话砍掉。
        String tail = unjudgedTail(vo);
        int budget = Math.max(0, CONCLUSION_MAX_LENGTH - tail.length());

        if (vo.getAbnormalCount() != null && vo.getAbnormalCount() == 0) {
            return truncate(String.format("%s：本次共 %d 项结果，均在参考范围内。",
                    nullToDash(vo.getLaboratoryItemName()), vo.getItemCount()), budget) + tail;
        }
        StringBuilder builder = new StringBuilder();
        if (vo.getCriticalCount() != null && vo.getCriticalCount() > 0) {
            builder.append("【危急值】");
            for (LabItemOverviewVO item : vo.getItems()) {
                if (Boolean.TRUE.equals(item.getCritical())) {
                    builder.append(item.getCriticalDesc()).append('；');
                }
            }
            builder.append('\n');
        }
        builder.append(String.format("%s：共 %d 项结果，其中 %d 项异常 —— ",
                nullToDash(vo.getLaboratoryItemName()), vo.getItemCount(), vo.getAbnormalCount()));
        for (LabItemOverviewVO item : vo.getItems()) {
            if (item.getAbnormalFlag() == null || item.getAbnormalFlag() == LabAbnormalJudge.NORMAL) {
                continue;
            }
            builder.append(item.getItemName()).append(item.getAbnormalFlagText()).append('、');
        }
        String text = builder.toString();
        return truncate(text.endsWith("、") ? text.substring(0, text.length() - 1) : text,
                budget) + tail;
    }

    /**
     * 「未判定」提示尾巴：没结论的结果必须被说出来，不能被读成正常。
     */
    private static String unjudgedTail(LabInterpretResultVO vo) {
        int count = vo.getUnjudgedCount() == null ? 0 : vo.getUnjudgedCount();
        if (count <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(String.format(
                "%n另有 %d 项未能自动判定（参考区间不可用），未纳入异常统计，需人工核对：", count));
        for (LabItemOverviewVO item : vo.getItems()) {
            if (LabAbnormalJudge.isUnjudged(item.getJudgeNote())) {
                builder.append(item.getItemName()).append('、');
            }
        }
        String text = builder.toString();
        return text.endsWith("、") ? text.substring(0, text.length() - 1) : text;
    }

    private static List<String> buildRuleSuggestions(LabInterpretResultVO vo) {
        List<String> suggestions = new ArrayList<>();
        if (vo.getCriticalCount() != null && vo.getCriticalCount() > 0) {
            suggestions.add("存在危急值，请立即按危急值流程确认接收并处置");
        }
        for (LabTrendVO trend : vo.getTrends()) {
            if ("升高".equals(trend.getDirection()) || "降低".equals(trend.getDirection())) {
                suggestions.add(String.format("%s呈持续%s趋势，建议结合临床复查",
                        trend.getItemName(), trend.getDirection()));
            }
            if (suggestions.size() >= MAX_SUGGESTIONS) {
                break;
            }
        }
        if (suggestions.isEmpty()) {
            suggestions.add("结果未见明显异常，建议结合临床表现综合判断");
        }
        if (vo.getUnjudgedCount() != null && vo.getUnjudgedCount() > 0
                && suggestions.size() < MAX_SUGGESTIONS) {
            suggestions.add(String.format("有 %d 项结果未能自动判定（参考区间不可用），请人工核对参考区间后复核",
                    vo.getUnjudgedCount()));
        }
        return suggestions;
    }

    private static List<String> buildRuleAttention(LabInterpretResultVO vo) {
        List<String> points = new ArrayList<>();
        for (LabItemOverviewVO item : vo.getItems()) {
            if (Boolean.TRUE.equals(item.getCritical())) {
                points.add(item.getCriticalDesc());
            }
        }
        if (points.isEmpty()) {
            for (LabItemOverviewVO item : vo.getItems()) {
                if (item.getAbnormalFlag() != null && item.getAbnormalFlag() != LabAbnormalJudge.NORMAL) {
                    points.add(String.format("%s%s：%s（参考 %s）", item.getItemName(),
                            item.getAbnormalFlagText(), item.getResultValue(),
                            nullToDash(item.getReferenceRange())));
                }
                if (points.size() >= MAX_ATTENTION_ITEMS) {
                    break;
                }
            }
        }
        return points;
    }

    private static List<String> toAttentionPoints(LabInterpretLlmOutputDTO output, List<LabItemOverviewVO> items) {
        List<String> points = new ArrayList<>();
        // 危急值永远排在前面，且不受模型输出影响
        for (LabItemOverviewVO item : items) {
            if (Boolean.TRUE.equals(item.getCritical())) {
                points.add(item.getCriticalDesc());
            }
        }
        if (output.getItems() == null) {
            return points;
        }
        for (LabInterpretLlmOutputDTO.Item raw : output.getItems()) {
            if (points.size() >= MAX_ATTENTION_ITEMS + 3) {
                break;
            }
            if (!StringUtils.hasText(raw.getItemName()) && !StringUtils.hasText(raw.getInterpretation())) {
                continue;
            }
            String level = raw.getLevel() == null ? "" : switch (raw.getLevel()) {
                case 3 -> "【尽快处理】";
                case 2 -> "【关注】";
                default -> "";
            };
            points.add(String.format("%s%s：%s", level, nullToDash(raw.getItemName()),
                    truncate(raw.getInterpretation(), 120)));
        }
        return points;
    }

    private static List<String> limitStrings(List<String> values, int max, int maxLength) {
        if (values == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (result.size() >= max) {
                break;
            }
            if (StringUtils.hasText(value)) {
                result.add(truncate(value, maxLength));
            }
        }
        return result;
    }

    private static String genderText(Integer gender) {
        if (gender == null) {
            return "（未填写）";
        }
        return gender == 1 ? "男" : gender == 2 ? "女" : "未知";
    }

    private static String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String value = text.trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String nullToDash(String text) {
        return StringUtils.hasText(text) ? text : "（未填写）";
    }

    public LabInterpretResultVO execute(LabInterpretExecuteDTO dto) {
        long start = System.currentTimeMillis();

        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("检验记录不存在：" + dto.getRecordId());
        }

        List<BizLabResult> results = loadResults(record.getId());
        if (results.isEmpty()) {
            throw new BusinessException("该检验记录尚未录入结果，无法解读：" + record.getRecordNo());
        }

        LabInterpretResultVO vo = new LabInterpretResultVO();
        vo.setRecordId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        vo.setPatientName(record.getPatientName());
        vo.setGenderText(genderText(record.getGender()));
        vo.setAge(record.getAge());
        vo.setLaboratoryItemName(record.getLaboratoryItemName());
        vo.setReportTime(record.getExecuteTime());

        // 规则层：逐项概览 + 危急值
        List<LabItemOverviewVO> items = buildOverview(results);
        vo.setItems(items);
        vo.setItemCount(items.size());
        vo.setAbnormalCount((int) items.stream()
                .filter(item -> item.getAbnormalFlag() != null && item.getAbnormalFlag() != LabAbnormalJudge.NORMAL)
                .count());
        vo.setCriticalCount((int) items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getCritical()))
                .count());
        // 未判定项既不算异常也不算正常。必须单独计数并显式说明，
        // 否则「共 8 项 / 7 项异常」会让读的人默认剩下那 1 项是正常的。
        vo.setUnjudgedCount((int) items.stream()
                .filter(item -> LabAbnormalJudge.isUnjudged(item.getJudgeNote()))
                .count());

        // 规则层：历史趋势
        boolean includeTrend = !Boolean.FALSE.equals(dto.getIncludeTrend());
        List<LabTrendVO> trends = includeTrend ? buildTrends(record, results) : List.of();
        vo.setTrends(trends);

        // 模型层
        Optional<LabInterpretLlmOutputDTO> llmOutput = callModel(record, items, trends);
        if (llmOutput.isPresent()) {
            LabInterpretLlmOutputDTO output = llmOutput.get();
            vo.setTrendSummary(truncate(output.getTrendSummary(), 300));
            vo.setConclusion(truncate(output.getConclusion(), CONCLUSION_MAX_LENGTH));
            vo.setSuggestions(limitStrings(output.getSuggestions(), MAX_SUGGESTIONS, 120));
            vo.setAttentionPoints(toAttentionPoints(output, items));
        } else {
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.LAB_INTERPRET)
                    + "；本次仅返回异常项与趋势的规则分析结果");
            vo.setConclusion(buildRuleConclusion(vo));
            vo.setSuggestions(buildRuleSuggestions(vo));
            vo.setAttentionPoints(buildRuleAttention(vo));
        }

        // 可选写回
        boolean overwrite = Boolean.TRUE.equals(dto.getOverwriteConclusion());
        if (overwrite && StringUtils.hasText(vo.getConclusion())) {
            vo.setConclusionSaved(writeBack(record, vo));
            if (Boolean.TRUE.equals(vo.getConclusionSaved())) {
                vo.setSaveTip("结论与建议已写回检验记录：" + record.getRecordNo()
                        + "（原内容已被覆盖）");
            } else {
                vo.setSaveTip("结论写回失败，草稿仍可在本结果中查看");
            }
        } else {
            vo.setConclusionSaved(false);
            vo.setSaveTip("结论为草稿，未写回检验记录（需显式开启覆盖才会写回）");
        }

        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    // 降级时的规则结论

    private List<BizLabResult> loadResults(Long recordId) {
        List<BizLabResult> results = labResultMapper.selectList(
                new LambdaQueryWrapper<BizLabResult>()
                        .eq(BizLabResult::getRecordId, recordId)
                        .orderByAsc(BizLabResult::getSortOrder)
                        .orderByAsc(BizLabResult::getId));
        return results == null ? List.of() : results;
    }

    private List<LabItemOverviewVO> buildOverview(List<BizLabResult> results) {
        List<LabItemOverviewVO> items = new ArrayList<>();
        for (BizLabResult result : results) {
            LabItemOverviewVO item = new LabItemOverviewVO();
            item.setItemName(result.getLaboratoryItemName());
            item.setResultValue(result.getResultValue());
            item.setResultUnit(result.getResultUnit());
            item.setReferenceRange(result.getReferenceRange());
            item.setAbnormalFlag(result.getAbnormalFlag());
            item.setAbnormalFlagText(LabAbnormalJudge.labelOf(result.getAbnormalFlag(), result.getJudgeNote()));
            item.setJudgeNote(result.getJudgeNote());

            Optional<LabCriticalValueRules.Hit> critical = LabCriticalValueRules.check(
                    result.getLaboratoryItemCode(), result.getLaboratoryItemName(),
                    result.getResultUnit(), result.getResultValue());
            item.setCritical(critical.isPresent());
            critical.ifPresent(hit -> item.setCriticalDesc(hit.description()));
            items.add(item);
        }
        return items;
    }

    /**
     * 历史趋势：同患者、同检验大项目的历史记录，按项目名逐项比较数值。
     * <p>
     * 只纳入能解析出数值的点 —— 定性结果（阴性/阳性）没有「升温降温」可言，
     * 把「阴性 → 阳性」硬算成趋势只会误导。
     */
    private List<LabTrendVO> buildTrends(BizLaboratoryRecord current, List<BizLabResult> currentResults) {
        List<BizLaboratoryRecord> history = laboratoryRecordMapper.selectList(
                new LambdaQueryWrapper<BizLaboratoryRecord>()
                        .eq(BizLaboratoryRecord::getPatientId, current.getPatientId())
                        .eq(current.getLaboratoryItemId() != null,
                                BizLaboratoryRecord::getLaboratoryItemId, current.getLaboratoryItemId())
                        .ne(BizLaboratoryRecord::getId, current.getId())
                        .isNotNull(BizLaboratoryRecord::getExecuteTime)
                        .orderByDesc(BizLaboratoryRecord::getExecuteTime)
                        .last("LIMIT " + TREND_HISTORY_LIMIT));
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        // 时间升序，让「首次」是真正最早的一次
        List<BizLaboratoryRecord> ordered = new ArrayList<>(history);
        ordered.add(current);
        ordered.sort(Comparator.comparing(
                BizLaboratoryRecord::getExecuteTime, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<Long, List<BizLabResult>> resultsByRecord = new HashMap<>();
        for (BizLaboratoryRecord record : ordered) {
            resultsByRecord.put(record.getId(),
                    record.getId().equals(current.getId()) ? currentResults : loadResults(record.getId()));
        }

        // 以「项目编码优先、否则项目名」作为跨记录的对齐键
        Map<String, LabTrendVO> trends = new LinkedHashMap<>();
        Map<String, List<LabTrendVO.Point>> pointsByKey = new LinkedHashMap<>();
        Map<String, String> labelByKey = new LinkedHashMap<>();
        Map<String, String> unitByKey = new LinkedHashMap<>();

        for (BizLaboratoryRecord record : ordered) {
            for (BizLabResult result : resultsByRecord.getOrDefault(record.getId(), List.of())) {
                Double numeric = LabReferenceRangeParser.firstNumber(result.getResultValue());
                if (numeric == null) {
                    continue;
                }
                String key = StringUtils.hasText(result.getLaboratoryItemCode())
                        ? result.getLaboratoryItemCode().trim().toUpperCase()
                        : String.valueOf(result.getLaboratoryItemName());
                labelByKey.putIfAbsent(key, result.getLaboratoryItemName());
                unitByKey.putIfAbsent(key, result.getResultUnit());

                LabTrendVO.Point point = new LabTrendVO.Point();
                point.setRecordNo(record.getRecordNo());
                point.setDate(record.getExecuteTime() == null
                        ? "" : record.getExecuteTime().format(DATE_FORMATTER));
                point.setResultValue(result.getResultValue());
                point.setNumericValue(numeric);
                point.setAbnormalFlag(result.getAbnormalFlag());
                pointsByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(point);
            }
        }

        for (Map.Entry<String, List<LabTrendVO.Point>> entry : pointsByKey.entrySet()) {
            List<LabTrendVO.Point> points = entry.getValue();
            if (points.size() < TREND_MIN_POINTS) {
                continue;
            }
            LabTrendVO trend = new LabTrendVO();
            trend.setItemName(labelByKey.get(entry.getKey()));
            trend.setUnit(unitByKey.get(entry.getKey()));
            trend.setPoints(points);

            double first = points.get(0).getNumericValue();
            double last = points.get(points.size() - 1).getNumericValue();
            trend.setDirection(directionOf(points, first, last));
            trend.setChangeText(points.stream()
                    .map(point -> formatNumber(point.getNumericValue()))
                    .reduce((a, b) -> a + " → " + b).orElse("")
                    + "（" + trend.getDirection() + "）");
            trend.setMagnitudeText(magnitudeText(first, last));
            trends.put(entry.getKey(), trend);
        }
        return new ArrayList<>(trends.values());
    }

    private Optional<LabInterpretLlmOutputDTO> callModel(BizLaboratoryRecord record,
                                                      List<LabItemOverviewVO> items,
                                                      List<LabTrendVO> trends) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("gender", genderText(record.getGender()));
        variables.put("age", record.getAge() == null ? "（未填写）" : record.getAge() + "岁");
        variables.put("itemName", nullToDash(record.getLaboratoryItemName()));
        variables.put("diagnosis", nullToDash(record.getDiagnosis()));
        variables.put("results", renderItems(items));
        variables.put("abnormalResults", renderAbnormal(items));
        variables.put("unjudgedResults", renderUnjudged(items));
        variables.put("criticalResults", renderCritical(items));
        variables.put("trends", renderTrends(trends));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.LAB_INTERPRET)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(record.getId())
                .inputDigest(nullToDash(record.getPatientName()) + " | "
                        + nullToDash(record.getLaboratoryItemName()) + " | 异常 "
                        + items.stream().filter(item -> item.getAbnormalFlag() != null
                        && item.getAbnormalFlag() != LabAbnormalJudge.NORMAL).count() + " 项")
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, LabInterpretLlmOutputDTO.class);
    }

    private String renderItems(List<LabItemOverviewVO> items) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (LabItemOverviewVO item : items) {
            if (index++ >= MAX_ITEMS_IN_PROMPT) {
                builder.append("…（其余结果省略）\n");
                break;
            }
            builder.append("- ").append(item.getItemName()).append(" = ")
                    .append(item.getResultValue());
            if (StringUtils.hasText(item.getResultUnit())) {
                builder.append(' ').append(item.getResultUnit());
            }
            builder.append("（参考 ").append(nullToDash(item.getReferenceRange())).append("，")
                    .append(item.getAbnormalFlagText()).append("）");
            if (StringUtils.hasText(item.getJudgeNote())) {
                builder.append(" [未判定：").append(item.getJudgeNote()).append(']');
            }
            builder.append('\n');
        }
        return builder.length() == 0 ? "（无）" : builder.toString();
    }

    private String renderAbnormal(List<LabItemOverviewVO> items) {
        StringBuilder builder = new StringBuilder();
        for (LabItemOverviewVO item : items) {
            if (item.getAbnormalFlag() == null || item.getAbnormalFlag() == LabAbnormalJudge.NORMAL) {
                continue;
            }
            builder.append("- ").append(item.getItemName()).append(' ')
                    .append(item.getAbnormalFlagText()).append("：")
                    .append(item.getResultValue());
            if (StringUtils.hasText(item.getResultUnit())) {
                builder.append(' ').append(item.getResultUnit());
            }
            builder.append("（参考 ").append(nullToDash(item.getReferenceRange())).append("）\n");
        }
        return builder.length() == 0 ? "（无异常项）" : builder.toString();
    }

    /**
     * 未判定项。必须单独喂给模型：模型看不到这些项时，
     * 「其余结果正常」这种话就会被它自己补出来。
     */
    private String renderUnjudged(List<LabItemOverviewVO> items) {
        StringBuilder builder = new StringBuilder();
        for (LabItemOverviewVO item : items) {
            if (LabAbnormalJudge.isUnjudged(item.getJudgeNote())) {
                builder.append("- ").append(item.getItemName()).append(" = ")
                        .append(item.getResultValue());
                if (StringUtils.hasText(item.getResultUnit())) {
                    builder.append(' ').append(item.getResultUnit());
                }
                builder.append("（").append(item.getJudgeNote()).append("）\n");
            }
        }
        return builder.length() == 0 ? "（无）" : builder.toString();
    }

    private String renderCritical(List<LabItemOverviewVO> items) {
        StringBuilder builder = new StringBuilder();
        for (LabItemOverviewVO item : items) {
            if (Boolean.TRUE.equals(item.getCritical())) {
                builder.append("- ").append(item.getCriticalDesc()).append('\n');
            }
        }
        return builder.length() == 0 ? "（无危急值）" : builder.toString();
    }

    private String renderTrends(List<LabTrendVO> trends) {
        if (trends.isEmpty()) {
            return "（无历史结果可比对）";
        }
        StringBuilder builder = new StringBuilder();
        for (LabTrendVO trend : trends) {
            builder.append("- ").append(trend.getItemName()).append("：")
                    .append(trend.getChangeText());
            if (StringUtils.hasText(trend.getUnit())) {
                builder.append(' ').append(trend.getUnit());
            }
            if (StringUtils.hasText(trend.getMagnitudeText())) {
                builder.append('，').append(trend.getMagnitudeText());
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    /**
     * 写回检验记录的结论与建议。只有调用方显式要求时才走到这里。
     */
    private boolean writeBack(BizLaboratoryRecord record, LabInterpretResultVO vo) {
        try {
            record.setDiagnosis(truncate(vo.getConclusion(), 2000));
            record.setSuggestions(vo.getSuggestions() == null || vo.getSuggestions().isEmpty()
                    ? null : String.join("\n", vo.getSuggestions()));
            record.setUpdateTime(LocalDateTime.now());
            laboratoryRecordMapper.updateById(record);
            return true;
        } catch (Exception ex) {
            log.error("[AI-检验解读] 结论写回失败，草稿仍正常返回", ex);
            return false;
        }
    }
}
