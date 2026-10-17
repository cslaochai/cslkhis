package com.his.common.enums;

import lombok.Getter;

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

    public static Icd10PredictSourceEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (Icd10PredictSourceEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案。
     */
    public static String getText(String code) {
        Icd10PredictSourceEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(String code) {
        Icd10PredictSourceEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
