package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 临床规则校验的错误级别枚举（1-提示 2-警告 3-严重）。
 * <p>
 * 3-严重 仅由确定性硬规则给出：模型返回该级别时一律降级，拦截权不交给概率系统。
 */
@Getter
public enum ClinicalRuleErrorLevelEnum {

    HINT(1, "提示"),
    WARN(2, "警告"),
    BLOCK(3, "严重");

    private final int code;
    private final String label;

    ClinicalRuleErrorLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static ClinicalRuleErrorLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ClinicalRuleErrorLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ClinicalRuleErrorLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ClinicalRuleErrorLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ClinicalRuleErrorLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
