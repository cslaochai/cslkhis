package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 临床规则校验的规则类型枚举（1-配伍禁忌 2-检验诊断关联性 3-用药合理性）
 */
@Getter
public enum ClinicalRuleTypeEnum {

    INCOMPATIBILITY(1, "配伍禁忌"),
    LAB_DIAGNOSIS_RELEVANCE(2, "检验诊断关联性"),
    MEDICATION_RATIONALITY(3, "用药合理性");

    private final int code;
    private final String label;

    ClinicalRuleTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static ClinicalRuleTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ClinicalRuleTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ClinicalRuleTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ClinicalRuleTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ClinicalRuleTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
