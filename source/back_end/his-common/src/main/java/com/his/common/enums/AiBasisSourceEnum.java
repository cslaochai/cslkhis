package com.his.common.enums;

import lombok.Getter;

/**
 * AI 结论判定依据来源枚举（字符串码值）：一条结论/建议是由确定性规则、模型、人工还是尚未给出。
 * <p>
 * 「拦截权」只属于 {@link #HARD_RULE}，模型给出的严重级别一律降级处理。
 */
@Getter
public enum AiBasisSourceEnum {

    HARD_RULE("HARD_RULE", "硬规则判定"),
    LLM("LLM", "模型判定"),
    MANUAL("MANUAL", "人工判定"),
    NONE("NONE", "无判定依据");

    private final String code;
    private final String label;

    AiBasisSourceEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AiBasisSourceEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AiBasisSourceEnum item : values()) {
            if (item.code.equals(code)) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(String code) {
        AiBasisSourceEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(String code) {
        AiBasisSourceEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
