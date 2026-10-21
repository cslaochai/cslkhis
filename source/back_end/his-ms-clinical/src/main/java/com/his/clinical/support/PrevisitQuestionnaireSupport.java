package com.his.clinical.support;

import com.his.common.enums.PrevisitQuestionTypeEnum;
import com.his.spi.vo.PrevisitQuestionnaireVO;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 预问诊量表
 *
 * <p>业务实现细节，只被本模块使用；题目结构类型 Option/Question 定义在跨域出参
 * {@link PrevisitQuestionnaireVO}（spi 契约），保证患者端小程序反序列化形状一致。
 */
@NoArgsConstructor
public final class PrevisitQuestionnaireSupport {

    /**
     * 量表版本：题目结构或选项有实质变化时递增
     */
    public static final String VERSION = "2026.07";

    /**
     * 主症状清单（覆盖门诊常见主诉；「其他」走自由文本兜底）
     */
    public static final List<PrevisitQuestionnaireVO.Option> MAIN_SYMPTOMS = List.of(
            new PrevisitQuestionnaireVO.Option("fever_cough", "发热/咳嗽/咽痛"),
            new PrevisitQuestionnaireVO.Option("dizzy_headache", "头晕/头痛"),
            new PrevisitQuestionnaireVO.Option("chest_pain", "胸闷/心悸/胸痛"),
            new PrevisitQuestionnaireVO.Option("abdominal", "腹痛/腹泻/恶心"),
            new PrevisitQuestionnaireVO.Option("joint_pain", "关节/腰腿疼痛"),
            new PrevisitQuestionnaireVO.Option("rash_itch", "皮疹/瘙痒"),
            new PrevisitQuestionnaireVO.Option("urinary", "尿频/尿急/尿痛"),
            new PrevisitQuestionnaireVO.Option("insomnia_weak", "乏力/失眠"),
            new PrevisitQuestionnaireVO.Option("other", "其他"));

    /**
     * 通用问（所有主症状都要答）
     */
    public static final List<PrevisitQuestionnaireVO.Question> COMMON_QUESTIONS = List.of(
            new PrevisitQuestionnaireVO.Question("onset", "症状持续多久了", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                    List.of("1天以内", "1-3天", "4-7天", "1-2周", "2周以上")),
            new PrevisitQuestionnaireVO.Question("trend", "最近病情变化", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                    List.of("加重", "无明显变化", "有所好转")),
            new PrevisitQuestionnaireVO.Question("medication", "是否自行用过药", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                    List.of("没用过", "用过且有效", "用过但没效果")),
            new PrevisitQuestionnaireVO.Question("medication_detail", "用过什么药（没用药可不填）", PrevisitQuestionTypeEnum.TEXT.getCode(), List.of()),
            new PrevisitQuestionnaireVO.Question("allergy", "是否有已知药物/食物过敏", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                    List.of("无", "有（请在补充描述里写明过敏物）")));

    /**
     * 主症状追问组（每症状 2 问，作答负荷最低的追问树起点）
     */
    public static final Map<String, List<PrevisitQuestionnaireVO.Question>> SYMPTOM_QUESTIONS = Map.of(
            "fever_cough", List.of(
                    new PrevisitQuestionnaireVO.Question("peak_temp", "最高体温大约多少", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("没量过", "37.3-38℃", "38.1-39℃", "39℃以上")),
                    new PrevisitQuestionnaireVO.Question("cough_type", "咳嗽形态", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("干咳", "有痰", "咽痛为主不咳"))),
            "dizzy_headache", List.of(
                    new PrevisitQuestionnaireVO.Question("dizzy_when", "什么情况下最明显", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("起床/起身时", "劳累后", "持续存在", "说不清")),
                    new PrevisitQuestionnaireVO.Question("head_site", "头痛位置", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("前额", "两侧太阳穴", "后枕部", "整个头"))),
            "chest_pain", List.of(
                    new PrevisitQuestionnaireVO.Question("chest_when", "胸闷/胸痛出现的时机", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("活动后", "静息时", "夜间", "情绪激动时")),
                    new PrevisitQuestionnaireVO.Question("chest_accompany", "是否伴随心慌/出冷汗", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("无心慌出汗", "有心慌", "有心慌+出冷汗"))),
            "abdominal", List.of(
                    new PrevisitQuestionnaireVO.Question("abdomen_site", "腹痛位置", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("上腹", "肚脐周围", "下腹", "右侧", "说不清")),
                    new PrevisitQuestionnaireVO.Question("abdomen_stool", "大便情况", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("正常", "腹泻", "便秘", "黑便/血便"))),
            "joint_pain", List.of(
                    new PrevisitQuestionnaireVO.Question("joint_site", "疼痛部位", PrevisitQuestionTypeEnum.TEXT.getCode(), List.of()),
                    new PrevisitQuestionnaireVO.Question("joint_morning", "晨起是否僵硬", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("无", "有，活动后缓解", "有，持续不缓解"))),
            "rash_itch", List.of(
                    new PrevisitQuestionnaireVO.Question("rash_site", "皮疹部位", PrevisitQuestionTypeEnum.TEXT.getCode(), List.of()),
                    new PrevisitQuestionnaireVO.Question("rash_trigger", "可能的诱因", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("新食物/药物", "接触新物品", "无明显诱因"))),
            "urinary", List.of(
                    new PrevisitQuestionnaireVO.Question("urine_symptom", "排尿伴随症状", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("尿频", "尿急", "尿痛", "尿频+尿痛")),
                    new PrevisitQuestionnaireVO.Question("urine_fever", "是否伴腰痛/发热", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("无", "腰痛", "发热", "腰痛+发热"))),
            "insomnia_weak", List.of(
                    new PrevisitQuestionnaireVO.Question("sleep_hours", "平均每晚睡眠", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("7小时以上", "5-7小时", "5小时以下")),
                    new PrevisitQuestionnaireVO.Question("weak_when", "乏力最明显的时段", PrevisitQuestionTypeEnum.CHOICE.getCode(),
                            List.of("晨起", "午后", "全天", "说不清"))),
            "other", List.of());
}
