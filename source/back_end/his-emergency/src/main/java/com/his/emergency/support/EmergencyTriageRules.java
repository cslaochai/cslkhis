package com.his.emergency.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 急诊分诊硬规则（纯代码，<b>绝不交给模型判断</b>）。
 * <p>
 * <b>为什么这里必须硬编码：</b> 分诊级别关系到「谁先被抢救」。生命体征达到红线
 * （SpO2 &lt; 90、SBP &lt; 90、GCS ≤ 8 …）时，级别是确定性的医学事实，
 * 不是需要「理解语义」的问题。把它交给概率模型，漏掉一次就是一条命。
 * <p>
 * 模型的用武之地在另一头：主诉文本里的语义线索（「压榨性胸痛伴大汗」→ 胸痛中心），
 * 以及体征没到红线但组合起来可疑的情况。所以本类只负责<b>确定性的下限</b>，
 * 模型可以在此基础上建议更严，但不能更松 —— 见
 * {@code EmergencyTriageCapability} 的「只升不降」合并逻辑。
 * <p>
 * <b>本类只产出「建议」，不写库、不覆盖人工分级。</b>
 */
public final class EmergencyTriageRules {

    /**
     * I 级濒危
     */
    public static final int LEVEL_CRITICAL = 1;

    /**
     * II 级危重
     */
    public static final int LEVEL_SEVERE = 2;

    /**
     * III 级急症
     */
    public static final int LEVEL_URGENT = 3;

    /**
     * IV 级非急症
     */
    public static final int LEVEL_NON_URGENT = 4;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Pattern TEXT_TEMP = Pattern.compile(
            "(?:T|体温)[:：]?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEXT_PULSE = Pattern.compile(
            "(?:P|HR|脉搏|心率)[:：]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEXT_RESP = Pattern.compile(
            "(?:R|RR|呼吸)[:：]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEXT_BP = Pattern.compile(
            "(?:BP|血压)[:：]?\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEXT_SPO2 = Pattern.compile(
            "(?:SpO2|SPO2|血氧饱和度|血氧)[:：]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEXT_GCS = Pattern.compile(
            "(?:GCS|格拉斯哥)[:：]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * 绿色通道关键词。命中即可显著缩短「识别 → 启动」的时间，
     * 这是分诊环节最有价值的一类确定性推断。
     */
    private static final List<ChannelRule> CHANNEL_RULES = List.of(
            new ChannelRule("胸痛中心",
                    List.of("胸痛", "胸闷", "胸骨后", "心前区", "压榨", "心梗", "心肌梗死"),
                    List.of("立即行床旁心电图并采血查心肌标志物",
                            "通知胸痛中心，评估再灌注治疗指征",
                            "建立静脉通道、心电监护")),
            new ChannelRule("卒中中心",
                    List.of("卒中", "中风", "偏瘫", "偏身", "口角歪斜", "言语不清",
                            "吐字不清", "肢体无力", "一侧无力", "口齿不清", "眩晕"),
                    List.of("准确记录发病时间（决定能否溶栓取栓）",
                            "启动卒中绿色通道，通知神经内科",
                            "评估血糖、血压并保持气道通畅")),
            new ChannelRule("创伤中心",
                    List.of("创伤", "车祸", "坠落", "摔伤", "刀伤", "刺伤", "外伤",
                            "骨折", "碾压", "撞击", "高处"),
                    List.of("按创伤机制评估，注意有无隐匿性出血",
                            "启动创伤绿色通道，评估生命体征与意识",
                            "止血、固定并建立静脉通道")));

    /**
     * 主诉里出现意识障碍类描述时的确定性红旗（这类描述不需要看体征也该升级）
     */
    private static final List<String> CONSCIOUSNESS_KEYWORDS = List.of(
            "意识不清", "昏迷", "呼之不应", "抽搐", "惊厥", "意识丧失", "不省人事", "昏睡");

    private EmergencyTriageRules() {
    }

    /**
     * 生命体征
     */
    public record VitalSigns(Double temperature,
                             Integer pulse,
                             Integer respiratory,
                             Integer systolic,
                             Integer diastolic,
                             Integer spo2,
                             Integer gcs,
                             Integer painScore) {

        public boolean isEmpty() {
            return temperature == null && pulse == null && respiratory == null
                    && systolic == null && diastolic == null && spo2 == null
                    && gcs == null && painScore == null;
        }

        public String describe() {
            if (isEmpty()) {
                return "（未采集生命体征）";
            }
            StringBuilder builder = new StringBuilder();
            if (temperature != null) {
                builder.append("体温").append(temperature).append("℃ ");
            }
            if (pulse != null) {
                builder.append("脉搏").append(pulse).append("次/分 ");
            }
            if (respiratory != null) {
                builder.append("呼吸").append(respiratory).append("次/分 ");
            }
            if (systolic != null) {
                builder.append("血压").append(systolic).append('/')
                        .append(diastolic == null ? '-' : diastolic).append(" mmHg ");
            }
            if (spo2 != null) {
                builder.append("血氧").append(spo2).append("% ");
            }
            if (gcs != null) {
                builder.append("GCS ").append(gcs).append(' ');
            }
            if (painScore != null) {
                builder.append("疼痛评分").append(painScore).append(' ');
            }
            return builder.toString().trim();
        }
    }

    /**
     * 红旗征象。<b>每条红旗都自带处置建议</b>，而不是只报一个级别 ——
     * 分诊护士真正需要的是「接下来做什么」，只给级别的提示几乎不会被采纳。
     *
     * @param label    征象描述
     * @param minLevel 该征象对应的最低严重级别（数值越小越严重）
     * @param action   对应的处置建议
     */
    public record RedFlag(String label, int minLevel, String action) {
    }

    private record ChannelRule(String channel, List<String> keywords, List<String> actions) {
    }

    // 解析

    /**
     * 解析生命体征。同时支持 JSON 与「T39.5 P130 BP80/50」这类文本 ——
     * 库里 {@code vital_signs} 是 TEXT 字段且实测为空，无法确定前端会写哪种格式，
     * 两种都认比要求改前端更现实。
     */
    public static VitalSigns parse(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new VitalSigns(null, null, null, null, null, null, null, null);
        }
        String text = raw.trim();
        if (text.startsWith("{")) {
            VitalSigns fromJson = parseJson(text);
            if (fromJson != null) {
                return fromJson;
            }
        }
        return parseText(text);
    }

    private static VitalSigns parseJson(String text) {
        try {
            JsonNode node = MAPPER.readTree(text);
            return new VitalSigns(
                    number(node, "temperature", "temp", "t", "tiwen"),
                    integer(node, "pulse", "hr", "heartRate", "heart_rate", "maibo"),
                    integer(node, "respiratory", "rr", "respiration", "breath", "huxi"),
                    integer(node, "systolic", "sbp", "systolicPressure", "highPressure", "shousuoya"),
                    integer(node, "diastolic", "dbp", "diastolicPressure", "lowPressure", "shuzhangya"),
                    integer(node, "spo2", "SpO2", "SPO2", "oxygenSaturation", "bloodOxygen", "xueyang"),
                    integer(node, "gcs", "GCS", "glasgow"),
                    integer(node, "painScore", "pain", "pain_score", "tengtong"));
        } catch (Exception ex) {
            return null;
        }
    }

    private static Double number(JsonNode node, String... keys) {
        JsonNode value = firstNode(node, keys);
        if (value == null) {
            return null;
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(value.asText(""));
        return matcher.find() ? Double.parseDouble(matcher.group()) : null;
    }

    private static Integer integer(JsonNode node, String... keys) {
        Double value = number(node, keys);
        return value == null ? null : (int) Math.round(value);
    }

    private static JsonNode firstNode(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    private static VitalSigns parseText(String text) {
        return new VitalSigns(
                firstDouble(TEXT_TEMP, text),
                firstInt(TEXT_PULSE, text),
                firstInt(TEXT_RESP, text),
                groupInt(TEXT_BP, text, 1),
                groupInt(TEXT_BP, text, 2),
                firstInt(TEXT_SPO2, text),
                firstInt(TEXT_GCS, text),
                null);
    }

    private static Double firstDouble(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : null;
    }

    private static Integer firstInt(Pattern pattern, String text) {
        Double value = firstDouble(pattern, text);
        return value == null ? null : (int) Math.round(value);
    }

    private static Integer groupInt(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(group));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // 红旗判定

    /**
     * 依据生命体征给出红旗征象。{@code minLevel} 的最小值即「硬性最低级别」。
     */
    public static List<RedFlag> evaluate(VitalSigns vitals) {
        List<RedFlag> flags = new ArrayList<>();
        if (vitals == null) {
            return flags;
        }

        Integer spo2 = vitals.spo2();
        if (spo2 != null) {
            if (spo2 < 90) {
                flags.add(new RedFlag("血氧饱和度 " + spo2 + "%（<90%）", LEVEL_CRITICAL,
                        "立即吸氧并持续监测血氧饱和度"));
            } else if (spo2 < 94) {
                flags.add(new RedFlag("血氧饱和度 " + spo2 + "%（<94%）", LEVEL_SEVERE,
                        "吸氧并复查血氧，评估呼吸衰竭风险"));
            }
        }

        Integer systolic = vitals.systolic();
        if (systolic != null) {
            if (systolic < 90) {
                flags.add(new RedFlag("收缩压 " + systolic + " mmHg（<90）", LEVEL_CRITICAL,
                        "建立静脉通道，按休克流程评估与补液"));
            } else if (systolic > 220) {
                flags.add(new RedFlag("收缩压 " + systolic + " mmHg（>220）", LEVEL_SEVERE,
                        "评估高血压急症，警惕靶器官损害"));
            }
        }
        Integer diastolic = vitals.diastolic();
        if (diastolic != null && diastolic > 130) {
            flags.add(new RedFlag("舒张压 " + diastolic + " mmHg（>130）", LEVEL_SEVERE,
                    "复测血压并评估高血压急症"));
        }

        Integer pulse = vitals.pulse();
        if (pulse != null) {
            if (pulse < 40 || pulse > 150) {
                flags.add(new RedFlag("心率 " + pulse + " 次/分（<40 或 >150）", LEVEL_CRITICAL,
                        "立即心电监护，评估血流动力学稳定性"));
            } else if (pulse < 50 || pulse > 120) {
                flags.add(new RedFlag("心率 " + pulse + " 次/分（<50 或 >120）", LEVEL_SEVERE,
                        "心电监护，查明心动过速/过缓原因"));
            }
        }

        Integer respiratory = vitals.respiratory();
        if (respiratory != null) {
            if (respiratory < 8 || respiratory > 30) {
                flags.add(new RedFlag("呼吸 " + respiratory + " 次/分（<8 或 >30）", LEVEL_CRITICAL,
                        "评估气道与呼吸支持需求，必要时辅助通气"));
            } else if (respiratory > 24) {
                flags.add(new RedFlag("呼吸 " + respiratory + " 次/分（>24）", LEVEL_SEVERE,
                        "监测呼吸频率变化，查找呼吸窘迫原因"));
            }
        }

        Double temperature = vitals.temperature();
        if (temperature != null) {
            if (temperature < 35 || temperature > 41) {
                flags.add(new RedFlag("体温 " + temperature + "℃（<35 或 >41）", LEVEL_CRITICAL,
                        "立即物理降温（高热）或复温（低体温）并查找病因"));
            } else if (temperature > 39.5) {
                flags.add(new RedFlag("体温 " + temperature + "℃（>39.5）", LEVEL_SEVERE,
                        "退热处理并评估感染灶"));
            }
        }

        Integer gcs = vitals.gcs();
        if (gcs != null) {
            if (gcs <= 8) {
                flags.add(new RedFlag("GCS " + gcs + " 分（≤8，昏迷）", LEVEL_CRITICAL,
                        "保持气道通畅，评估气管插管指征"));
            } else if (gcs <= 12) {
                flags.add(new RedFlag("GCS " + gcs + " 分（≤12）", LEVEL_SEVERE,
                        "评估意识障碍原因，观察瞳孔与肢体活动"));
            }
        }

        Integer painScore = vitals.painScore();
        if (painScore != null && painScore >= 8) {
            flags.add(new RedFlag("疼痛评分 " + painScore + " 分（≥8）", LEVEL_SEVERE,
                    "评估疼痛性质与部位，排查致命性胸腹痛"));
        }
        return flags;
    }

    /**
     * 主诉文本的确定性红旗：意识障碍类描述无需等体征也该升级
     */
    public static List<RedFlag> evaluateChiefComplaint(String chiefComplaint) {
        List<RedFlag> flags = new ArrayList<>();
        if (!StringUtils.hasText(chiefComplaint)) {
            return flags;
        }
        String text = chiefComplaint.replaceAll("\\s+", "");
        for (String keyword : CONSCIOUSNESS_KEYWORDS) {
            if (text.contains(keyword)) {
                flags.add(new RedFlag("主诉提示意识障碍：「" + keyword + "」", LEVEL_CRITICAL,
                        "立即评估气道、呼吸、循环与意识，按昏迷流程处置"));
                break;
            }
        }
        return flags;
    }

    /**
     * 按红旗征象取硬性最低级别，无红旗时返回 null（不干预）
     */
    public static Integer hardLevelOf(List<RedFlag> flags) {
        if (flags == null || flags.isEmpty()) {
            return null;
        }
        int level = LEVEL_NON_URGENT;
        for (RedFlag flag : flags) {
            level = Math.min(level, flag.minLevel());
        }
        return level;
    }

    // 绿色通道

    /**
     * 依据主诉文本识别绿色通道，识别不到返回无
     */
    public static String detectGreenChannel(String chiefComplaint) {
        if (!StringUtils.hasText(chiefComplaint)) {
            return "无";
        }
        String text = chiefComplaint.replaceAll("\\s+", "");
        for (ChannelRule rule : CHANNEL_RULES) {
            for (String keyword : rule.keywords()) {
                if (text.contains(keyword)) {
                    return rule.channel();
                }
            }
        }
        return "无";
    }

    /**
     * 该绿色通道对应的启动动作
     */
    public static List<String> channelActions(String channel) {
        if (!StringUtils.hasText(channel)) {
            return List.of();
        }
        for (ChannelRule rule : CHANNEL_RULES) {
            if (rule.channel().equals(channel)) {
                return rule.actions();
            }
        }
        return List.of();
    }

    /**
     * 是否为合法的绿色通道取值（用于校验模型输出）
     */
    public static boolean isValidChannel(String channel) {
        if (!StringUtils.hasText(channel)) {
            return false;
        }
        if ("无".equals(channel.trim())) {
            return true;
        }
        return CHANNEL_RULES.stream().anyMatch(rule -> rule.channel().equals(channel.trim()));
    }

    public static String levelText(Integer level) {
        if (level == null) {
            return "未定级";
        }
        return switch (level) {
            case LEVEL_CRITICAL -> "I级 濒危";
            case LEVEL_SEVERE -> "II级 危重";
            case LEVEL_URGENT -> "III级 急症";
            case LEVEL_NON_URGENT -> "IV级 非急症";
            default -> "未知级别";
        };
    }

    /**
     * 级别 → 区域。刻意用推导而不是取模型给的区域字符串 ——
     * 级别与区域是同一件事的两种表达，让两者由不同来源给出必然出现自相矛盾。
     */
    public static String zoneOf(Integer level) {
        if (level == null) {
            return null;
        }
        return switch (level) {
            case LEVEL_CRITICAL, LEVEL_SEVERE -> "红区";
            case LEVEL_URGENT -> "黄区";
            default -> "绿区";
        };
    }
}
