package com.his.common.support;

import com.his.common.enums.EmergencyConsciousnessEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 急诊 MEWS 评分（改良早期预警评分）—— 分诊评分的单一出处。
 *
 * <p>规则逐格对齐 WS/T 810—2022《基层医疗卫生机构急重患者判断及转诊技术标准》
 * 表 1「改良早期预警评分(MEWS)」：心率 / 收缩压 / 呼吸频率 / 体温 / 意识 五项，
 * 每项 0~3 分，总分 0~14 分。分层口径取该标准 5.1.3：
 * <ul>
 *   <li>总分 ≥ 9 → 极高危；</li>
 *   <li>5 ≤ 总分 &lt; 9 → 高危；</li>
 *   <li>总分 &lt; 5 → 平诊。</li>
 * </ul>
 *
 * <p><b>为什么不能拿 MEWS 当唯一判据</b>：meta 分析（王嘉颖等，中国医药导报 2025）
 * 显示 MEWS 识别潜在危重症的合并灵敏度仅约 0.72——固定截断值会漏掉「沉默的危重」。
 * 所以本评分只做分诊辅助：AI 红旗硬规则（{@link EmergencyTriageRules}）与人工分级
 * 仍然并行，MEWS 绝不自动改分诊级别。
 *
 * <p><b>缺项口径</b>：体征/意识缺项按 0 分计（不编造），但 {@link MewsResult#consciousnessMissing()}
 * 会显式标记「意识未采」——缺项计 0 会压低总分、低估危险性，界面必须把这件事说出来。
 */
public final class EmergencyMewsRules {

    private EmergencyMewsRules() {
    }

    /**
     * MEWS 分层（文本即对外口径，调用方不要自己拼）
     */
    public static final String LEVEL_EXTREMELY_HIGH = "极高危";
    public static final String LEVEL_HIGH = "高危";
    public static final String LEVEL_ROUTINE = "平诊";

    /**
     * 评分入参：生命体征（复用 AI 分诊的解析结果，保持「一处采集两处消费」）
     */
    public record MewsInput(EmergencyTriageRules.VitalSigns vitals,
                            Integer consciousness) {
        public MewsInput {
            if (vitals == null) {
                vitals = new EmergencyTriageRules.VitalSigns(null, null, null, null, null, null, null, null);
            }
        }
    }

    /**
     * 单项评分明细（供界面逐项展示：哪一项在扣分，一眼可见）
     */
    public record MewsItem(String name, String valueText, int score) {
    }

    /**
     * 评分结果。score 为权威总分；levelText 为分层；consciousnessMissing 标记必采项缺失。
     */
    public record MewsResult(int score, String levelText, boolean consciousnessMissing,
                             List<MewsItem> items) {

        public static MewsResult empty() {
            return new MewsResult(0, LEVEL_ROUTINE, true, List.of());
        }

        /**
         * 分层文本（总分 → 极高危/高危/平诊）
         */
        public static String levelOf(int score) {
            if (score >= 9) {
                return LEVEL_EXTREMELY_HIGH;
            }
            return score >= 5 ? LEVEL_HIGH : LEVEL_ROUTINE;
        }
    }

    /**
     * 心率（次/min）：≤40 → 3；41~50 → 2；51~100 → 0；101~110 → 1；111~129 → 2；≥130 → 3。
     */
    public static int pulseScore(Integer pulse) {
        if (pulse == null) {
            return 0;
        }
        if (pulse <= 40) {
            return 3;
        }
        if (pulse <= 50) {
            return 2;
        }
        if (pulse <= 100) {
            return 0;
        }
        if (pulse <= 110) {
            return 1;
        }
        return pulse <= 129 ? 2 : 3;
    }

    /**
     * 收缩压（mmHg）：≤70 → 3；71~80 → 2；81~100 → 1；101~199 → 0；≥200 → 2。
     */
    public static int systolicScore(Integer systolic) {
        if (systolic == null) {
            return 0;
        }
        if (systolic <= 70) {
            return 3;
        }
        if (systolic <= 80) {
            return 2;
        }
        if (systolic <= 100) {
            return 1;
        }
        return systolic >= 200 ? 2 : 0;
    }

    /**
     * 呼吸频率（次/min）：&lt;9 → 3；9~14 → 0；15~20 → 1；21~29 → 2；≥30 → 3。
     */
    public static int respiratoryScore(Integer respiratory) {
        if (respiratory == null) {
            return 0;
        }
        if (respiratory < 9) {
            return 3;
        }
        if (respiratory <= 14) {
            return 0;
        }
        if (respiratory <= 20) {
            return 1;
        }
        return respiratory <= 29 ? 2 : 3;
    }

    /**
     * 体温（℃）：&lt;35.0 → 2；35.0~38.4 → 0；≥38.5 → 2。
     */
    public static int temperatureScore(Double temperature) {
        if (temperature == null) {
            return 0;
        }
        if (temperature < 35.0 || temperature >= 38.5) {
            return 2;
        }
        return 0;
    }

    /**
     * 意识：清楚 → 0；对声音有反应 → 1；对疼痛有反应 → 2；无反应 → 3。
     * 缺项（null 或脏码值）按 0 分计，由调用方经 {@link MewsResult#consciousnessMissing()} 显式暴露。
     */
    public static int consciousnessScore(Integer consciousness) {
        EmergencyConsciousnessEnum item = EmergencyConsciousnessEnum.fromCode(consciousness);
        return item == null ? 0 : item.getCode() - 1;
    }

    /**
     * 完整评分：五项累加 + 分层 + 缺项标记 + 逐项明细。
     */
    public static MewsResult score(MewsInput input) {
        if (input == null) {
            return MewsResult.empty();
        }
        EmergencyTriageRules.VitalSigns vitals = input.vitals();
        Integer consciousness = input.consciousness();
        boolean consciousnessMissing = !EmergencyConsciousnessEnum.isValid(consciousness);

        List<MewsItem> items = new ArrayList<>(5);
        int pulseScore = pulseScore(vitals.pulse());
        items.add(new MewsItem("心率", textOf(vitals.pulse(), "次/分"), pulseScore));
        int systolic = systolicScore(vitals.systolic());
        items.add(new MewsItem("收缩压", textOf(vitals.systolic(), "mmHg"), systolic));
        int respiratory = respiratoryScore(vitals.respiratory());
        items.add(new MewsItem("呼吸", textOf(vitals.respiratory(), "次/分"), respiratory));
        int temperature = temperatureScore(vitals.temperature());
        items.add(new MewsItem("体温", textOf(vitals.temperature(), "℃"), temperature));
        int consciousnessScore = consciousnessScore(consciousness);
        items.add(new MewsItem("意识",
                EmergencyConsciousnessEnum.getLabel(consciousness), consciousnessScore));

        int total = pulseScore + systolic + respiratory + temperature + consciousnessScore;
        return new MewsResult(total, MewsResult.levelOf(total), consciousnessMissing, items);
    }

    private static String textOf(Number value, String unit) {
        if (value == null) {
            return "未采";
        }
        // 体温可能是 38.0，整数显示不带小数点更贴护士习惯
        if (value instanceof Double d) {
            return (d == Math.floor(d) ? (long) d.doubleValue() : d) + unit;
        }
        return value + unit;
    }
}
