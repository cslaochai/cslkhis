package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return code != null && fromCode(code) != null;
    }

    public static AiBasisSourceEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (AiBasisSourceEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        AiBasisSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        AiBasisSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        AiBasisSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
