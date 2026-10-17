package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * AI 能力的表达层来源枚举（model-模型串话 rule-规则兜底）。
 * <p>
 * 这是出参展示码，前端据此决定要不要挂「AI 生成」标识 —— 不能把规则文案说成模型说的，那是虚标。
 * 与「结论判定依据来源」（硬规则/模型/人工）不是同一口径：那回答的是「这条判断谁给的」，
 * 这里回答的是「这段话谁写的」，硬规则给出的结论也可以由模板渲染。
 */
@Getter
public enum AiExpressionSourceEnum {

    RULE("rule", "规则文案"),
    MODEL("model", "模型生成");

    private final String code;
    private final String label;

    AiExpressionSourceEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    public static AiExpressionSourceEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (AiExpressionSourceEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        AiExpressionSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        AiExpressionSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        AiExpressionSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
