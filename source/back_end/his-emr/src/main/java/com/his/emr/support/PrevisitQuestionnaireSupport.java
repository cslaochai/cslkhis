package com.his.emr.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 预问诊量表（G-05）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrevisitQuestionnaireSupport {

    /**
     * 量表版本：题目结构或选项有实质变化时递增
     */
    public static final String VERSION = "2026.07";

    /**
     * 题型：单选
     */
    public static final String TYPE_CHOICE = "choice";

    /**
     * 题型：文本
     */
    public static final String TYPE_TEXT = "text";

    /**
     * 主症状清单（覆盖门诊常见主诉；「其他」走自由文本兜底）
     */
    public static final List<Option> MAIN_SYMPTOMS = List.of(
            new Option("fever_cough", "发热/咳嗽/咽痛"),
            new Option("dizzy_headache", "头晕/头痛"),
            new Option("chest_pain", "胸闷/心悸/胸痛"),
            new Option("abdominal", "腹痛/腹泻/恶心"),
            new Option("joint_pain", "关节/腰腿疼痛"),
            new Option("rash_itch", "皮疹/瘙痒"),
            new Option("urinary", "尿频/尿急/尿痛"),
            new Option("insomnia_weak", "乏力/失眠"),
            new Option("other", "其他"));

    /**
     * 通用问（所有主症状都要答）
     */
    public static final List<Question> COMMON_QUESTIONS = List.of(
            new Question("onset", "症状持续多久了", TYPE_CHOICE,
                    List.of("1天以内", "1-3天", "4-7天", "1-2周", "2周以上")),
            new Question("trend", "最近病情变化", TYPE_CHOICE,
                    List.of("加重", "无明显变化", "有所好转")),
            new Question("medication", "是否自行用过药", TYPE_CHOICE,
                    List.of("没用过", "用过且有效", "用过但没效果")),
            new Question("medication_detail", "用过什么药（没用药可不填）", TYPE_TEXT, List.of()),
            new Question("allergy", "是否有已知药物/食物过敏", TYPE_CHOICE,
                    List.of("无", "有（请在补充描述里写明过敏物）")));

    /**
     * 主症状追问组（每症状 2 问，作答负荷最低的追问树起点）
     */
    public static final Map<String, List<Question>> SYMPTOM_QUESTIONS = Map.of(
            "fever_cough", List.of(
                    new Question("peak_temp", "最高体温大约多少", TYPE_CHOICE,
                            List.of("没量过", "37.3-38℃", "38.1-39℃", "39℃以上")),
                    new Question("cough_type", "咳嗽形态", TYPE_CHOICE,
                            List.of("干咳", "有痰", "咽痛为主不咳"))),
            "dizzy_headache", List.of(
                    new Question("dizzy_when", "什么情况下最明显", TYPE_CHOICE,
                            List.of("起床/起身时", "劳累后", "持续存在", "说不清")),
                    new Question("head_site", "头痛位置", TYPE_CHOICE,
                            List.of("前额", "两侧太阳穴", "后枕部", "整个头"))),
            "chest_pain", List.of(
                    new Question("chest_when", "胸闷/胸痛出现的时机", TYPE_CHOICE,
                            List.of("活动后", "静息时", "夜间", "情绪激动时")),
                    new Question("chest_accompany", "是否伴随心慌/出冷汗", TYPE_CHOICE,
                            List.of("无心慌出汗", "有心慌", "有心慌+出冷汗"))),
            "abdominal", List.of(
                    new Question("abdomen_site", "腹痛位置", TYPE_CHOICE,
                            List.of("上腹", "肚脐周围", "下腹", "右侧", "说不清")),
                    new Question("abdomen_stool", "大便情况", TYPE_CHOICE,
                            List.of("正常", "腹泻", "便秘", "黑便/血便"))),
            "joint_pain", List.of(
                    new Question("joint_site", "疼痛部位", TYPE_TEXT, List.of()),
                    new Question("joint_morning", "晨起是否僵硬", TYPE_CHOICE,
                            List.of("无", "有，活动后缓解", "有，持续不缓解"))),
            "rash_itch", List.of(
                    new Question("rash_site", "皮疹部位", TYPE_TEXT, List.of()),
                    new Question("rash_trigger", "可能的诱因", TYPE_CHOICE,
                            List.of("新食物/药物", "接触新物品", "无明显诱因"))),
            "urinary", List.of(
                    new Question("urine_symptom", "排尿伴随症状", TYPE_CHOICE,
                            List.of("尿频", "尿急", "尿痛", "尿频+尿痛")),
                    new Question("urine_fever", "是否伴腰痛/发热", TYPE_CHOICE,
                            List.of("无", "腰痛", "发热", "腰痛+发热"))),
            "insomnia_weak", List.of(
                    new Question("sleep_hours", "平均每晚睡眠", TYPE_CHOICE,
                            List.of("7小时以上", "5-7小时", "5小时以下")),
                    new Question("weak_when", "乏力最明显的时段", TYPE_CHOICE,
                            List.of("晨起", "午后", "全天", "说不清"))),
            "other", List.of());

    /**
     * 选项/题目
     */
    public record Option(String code, String label) {
    }

    /**
     * 问题
     */
    public record Question(String key, String label, String type, List<String> options) {
    }
}
