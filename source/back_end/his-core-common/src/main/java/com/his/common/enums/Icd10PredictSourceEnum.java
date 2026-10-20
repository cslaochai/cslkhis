package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 诊断编码预测项来源枚举（LLM-模型推荐 RULE-规则降级）。
 * <p>
 * 模型不可用时整批降级到规则词典，前端按这一档标出「不是模型给的」。
 * 与「结论判定依据来源」不是一个口径：那里的硬规则档叫 HARD_RULE，这里的 RULE 指的是词典降级，
 * 码值也不同（本列写 RULE，不写 HARD_RULE），混用会让出参标识直接失配。
 */
@Getter
public enum Icd10PredictSourceEnum {

    LLM("LLM", "模型推荐"),
    RULE("RULE", "规则降级");

    private final String code;
    private final String label;

    Icd10PredictSourceEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    public static Icd10PredictSourceEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (Icd10PredictSourceEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        Icd10PredictSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        Icd10PredictSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        Icd10PredictSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
