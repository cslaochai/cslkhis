package com.his.ai.support;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 病历字段白名单（P1-3 结构化抽取的反幻觉底座）。
 * <p>
 * <b>为什么必须有这张表</b>：抽取的产出最终会被填进门诊病历的正式字段。
 * 如果字段名由模型自由发挥（返回个「月经史」「家族遗传病」），前端既不知道怎么渲染，
 * 也压根没有对应的列可写 —— 这和 ICD 编码必须落在候选集内是同一类约束。
 * 因此：<b>模型只能从本表给出的 key 里选，白名单外的字段一律丢弃并计数</b>。
 * <p>
 * 别名表同时被硬规则层用来做「标签切分」：医生粘贴的文本常常本身就是
 * 主诉：咳嗽3天这种带标签的格式（从其他系统/模板抄来的），
 * 这类内容用规则逐字切分比让模型改写更准 —— 也顺带成为模型不可用时的兜底路径。
 */
public final class EmrFieldCatalog {

    private static final List<TextField> TEXT_FIELDS = List.of(
            new TextField("chiefComplaint", "主诉", List.of("主诉")),
            new TextField("presentIllness", "现病史", List.of("现病史", "病史")),
            new TextField("pastHistory", "既往史", List.of("既往病史", "既往史", "过去史", "既往情况")),
            new TextField("personalHistory", "个人史", List.of("个人史")),
            new TextField("familyHistory", "家族史", List.of("家族病史", "家族史")),
            new TextField("allergyHistory", "过敏史", List.of("药物过敏史", "过敏史", "过敏")),
            new TextField("generalCondition", "一般情况", List.of("一般情况", "一般状况", "全身情况")),
            new TextField("skinMucosa", "皮肤黏膜", List.of("皮肤黏膜", "皮肤粘膜", "皮肤")),
            new TextField("headNeck", "头颈部", List.of("头颈部", "头颈", "颈部", "头颅")),
            new TextField("chestLung", "胸肺部", List.of("胸肺部", "胸肺", "双肺", "肺部", "胸部")),
            new TextField("heart", "心脏", List.of("心脏", "心律", "心率")),
            new TextField("abdomen", "腹部", List.of("腹部", "腹软")),
            new TextField("spineLimbs", "脊柱四肢", List.of("脊柱四肢", "脊柱", "四肢")),
            new TextField("nervousSystem", "神经系统", List.of("神经系统", "病理反射", "神经反射")),
            new TextField("specialistExam", "专科检查", List.of("专科检查", "专科查体", "专科情况")),
            new TextField("auxiliaryExam", "辅助检查", List.of("辅助检查结果", "辅助检查", "辅查", "检查结果")),
            new TextField("diagnosis", "诊断", List.of("初步诊断", "门诊诊断", "临床诊断", "诊断")),
            new TextField("treatmentPlan", "处理意见", List.of("处理意见", "治疗方案", "处置", "处理")));
    /**
     * <b>模型不可写的字段：诊断与处理意见。</b>
     * <p>
     * 这两个字段装的是<b>诊疗决策</b>，不是"对已有信息的搬运"：诊断权在医生，
     * 处理意见是有法律效力的文书，责任在开立者。让模型往这两个字段写字，
     * 风险等级和「凭空多一句阴性描述」完全不是一个量级 ——
     * 一条被"顺"出来的诊断，落到病历上就是一次医疗差错。
     * <p>
     * 所以它们<b>仍在白名单里</b>（原文带「诊断：」标签时要能被逐字切分、前端要能渲染），
     * 但 {@code EmrExtractCapability} 会把模型给这两个字段的产出整条丢弃，
     * 并且 {@link #writableFieldPrompt()} 不会把它们列给模型看。
     */
    private static final Set<String> LLM_READONLY_KEYS = Set.of("diagnosis", "treatmentPlan");
    /**
     * 血压是「一个标签两个数」，单独处理，不放进 {@link #VITAL_FIELDS}
     */
    private static final Pattern BLOOD_PRESSURE =
            Pattern.compile("(?:血压|(?<![A-Za-z])BP(?![A-Za-z]))[^\\d\\n]{0,4}?(\\d{2,3})\\s*[/／]\\s*(\\d{2,3})");
    /**
     * 体征正则的写法说明（改这块前先读）：
     * <ul>
     *   <li>中文全称（体温/脉搏/呼吸/血压）后面允许夹 0~4 个非数字字符，
     *       这样「体温最高38.5℃」「脉搏约125次/分」也能抽到；</li>
     *   <li>单字母缩写（T/P/R/BP）**不允许**夹字母，且要求左右都不是字母 ——
     *       否则「CRP 5」会被当成呼吸 5、「PSA 3.5」会被当成脉搏。
     *       这是真实存在的一类误匹配，靠正则的边界断言挡掉，不要图省事删掉 lookaround；</li>
     *   <li>最后仍有 {@code min/max} 取值范围兜底（呼吸 5~60 之类的常识区间）。</li>
     * </ul>
     * 即便三道都过了，抽出来的体征也只是**候选值**，由医生逐字段采纳后才写进病历，
     * 所以误匹配的代价是「医生多看一眼」，不是「记错一条记录」。
     */
    private static final List<VitalField> VITAL_FIELDS = List.of(
            new VitalField("temperature", "体温", "℃",
                    Pattern.compile("(?:体温[^\\d\\n]{0,4}?|(?<![A-Za-z])T(?![A-Za-z])[^\\d\\n]{0,2}?)(\\d{2}(?:\\.\\d)?)"),
                    30, 45),
            new VitalField("pulse", "脉搏", "次/分",
                    Pattern.compile("(?:脉搏[^\\d\\n]{0,4}?|(?<![A-Za-z])P(?![A-Za-z])[^\\d\\n]{0,2}?)(\\d{2,3})"),
                    20, 250),
            new VitalField("respiration", "呼吸", "次/分",
                    Pattern.compile("(?:呼吸[^\\d\\n]{0,4}?|(?<![A-Za-z])R(?![A-Za-z])[^\\d\\n]{0,2}?)(\\d{1,2})"),
                    5, 60));
    private static final int SYSTOLIC_MIN = 40;
    private static final int SYSTOLIC_MAX = 300;
    private static final int DIASTOLIC_MIN = 20;
    private static final int DIASTOLIC_MAX = 200;
    private static final Map<String, TextField> TEXT_BY_KEY = new LinkedHashMap<>();
    /**
     * 行首标签 → 字段，按标签长度降序（保证「既往病史」先于「病史」命中）
     */
    private static final List<Map.Entry<String, TextField>> SORTED_ALIASES = new ArrayList<>();

    static {
        for (TextField field : TEXT_FIELDS) {
            TEXT_BY_KEY.put(field.key(), field);
            for (String alias : field.aliases()) {
                SORTED_ALIASES.add(Map.entry(alias, field));
            }
        }
        SORTED_ALIASES.sort(Comparator.comparingInt((Map.Entry<String, TextField> e) -> e.getKey().length()).reversed());
    }

    private EmrFieldCatalog() {
    }

    public static List<TextField> textFields() {
        return TEXT_FIELDS;
    }

    public static List<VitalField> vitalFields() {
        return VITAL_FIELDS;
    }

    public static List<Map.Entry<String, TextField>> sortedAliases() {
        return SORTED_ALIASES;
    }

    public static Pattern bloodPressurePattern() {
        return BLOOD_PRESSURE;
    }

    public static int systolicMin() {
        return SYSTOLIC_MIN;
    }

    public static int systolicMax() {
        return SYSTOLIC_MAX;
    }

    public static int diastolicMin() {
        return DIASTOLIC_MIN;
    }

    public static int diastolicMax() {
        return DIASTOLIC_MAX;
    }

    /**
     * 白名单校验：key 是否是可写的文本字段
     */
    public static boolean isTextKey(String key) {
        return StringUtils.hasText(key) && TEXT_BY_KEY.containsKey(key.trim());
    }

    /**
     * 模型是否可以写这个字段 = 在白名单内且不是诊疗决策字段（诊断 / 处理意见）。
     * <p>
     * 与 {@link #isTextKey} 的区别是刻意的：{@code diagnosis} 要能被硬规则层切分、
     * 要能被前端渲染，但<b>不能接受模型产出</b>。
     */
    public static boolean isLlmWritable(String key) {
        return isTextKey(key) && !LLM_READONLY_KEYS.contains(key.trim());
    }

    /**
     * 白名单校验：key 是否是可写的体征字段
     */
    public static boolean isVitalKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        String value = key.trim();
        return "systolicPressure".equals(value) || "diastolicPressure".equals(value)
                || VITAL_FIELDS.stream().anyMatch(field -> field.key().equals(value));
    }

    public static String getText(String key) {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        TextField text = TEXT_BY_KEY.get(key.trim());
        if (text != null) {
            return text.label();
        }
        return switch (key.trim()) {
            case "temperature" -> "体温";
            case "pulse" -> "脉搏";
            case "respiration" -> "呼吸";
            case "systolicPressure" -> "收缩压";
            case "diastolicPressure" -> "舒张压";
            default -> key.trim();
        };
    }

    /**
     * 白名单的取值清单，写进提示词，让模型看到可选字段。
     * <p>
     * 诊疗决策字段（诊断 / 处理意见）不列给模型 —— 列了就是在暗示它可以写。
     */
    public static String writableFieldPrompt() {
        StringBuilder builder = new StringBuilder();
        for (TextField field : TEXT_FIELDS) {
            if (LLM_READONLY_KEYS.contains(field.key())) {
                continue;
            }
            builder.append("  ").append(field.key()).append("（").append(field.label()).append("）\n");
        }
        for (VitalField field : VITAL_FIELDS) {
            builder.append("  ").append(field.key()).append("（").append(field.label()).append("，单位")
                    .append(field.unit()).append("）\n");
        }
        builder.append("  systolicPressure（收缩压，单位mmHg）\n");
        builder.append("  diastolicPressure（舒张压，单位mmHg）\n");
        return builder.toString();
    }

    /**
     * 血压提取，返回 [收缩压, 舒张压]，未匹配或超出合理范围返回 null
     */
    public static String[] extractBloodPressure(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        Matcher matcher = BLOOD_PRESSURE.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        int systolic = Integer.parseInt(matcher.group(1));
        int diastolic = Integer.parseInt(matcher.group(2));
        if (systolic < SYSTOLIC_MIN || systolic > SYSTOLIC_MAX
                || diastolic < DIASTOLIC_MIN || diastolic > DIASTOLIC_MAX) {
            return null;
        }
        return new String[]{String.valueOf(systolic), String.valueOf(diastolic)};
    }

    /**
     * 文本类病历字段
     *
     * @param key     与 {@code BizMedicalRecord} 的字段名一致，前端据此写表单
     * @param label   中文名，用于展示与提示词
     * @param aliases 行首标签别名（用于硬规则切分），按长度降序匹配
     */
    public record TextField(String key, String label, List<String> aliases) {
    }

    /**
     * 体征字段（数值 + 单位）
     * <p>
     * 体征用正则抽比用模型抽可靠得多，且误匹配可以用取值范围兜住，
     * 所以这一组<b>只走硬规则，不交给模型</b>。
     *
     * @param key     与 {@code BizMedicalRecord} 的字段名一致
     * @param label   中文名
     * @param unit    单位
     * @param pattern 提取正则，第 1 个捕获组为数值
     * @param min     合理下限（含），用于挡掉误匹配
     * @param max     合理上限（含）
     */
    public record VitalField(String key, String label, String unit,
                             Pattern pattern, double min, double max) {
    }
}
