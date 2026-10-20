package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品相互作用严重度枚举（1-禁忌 2-慎用；与字典 his_drug_interaction_severity 同码同义）
 */
@Getter
public enum DrugInteractionSeverityEnum {

    FORBIDDEN(1, "禁忌"),
    CAUTION(2, "慎用");

    private final int code;
    private final String label;

    DrugInteractionSeverityEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DrugInteractionSeverityEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DrugInteractionSeverityEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DrugInteractionSeverityEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DrugInteractionSeverityEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DrugInteractionSeverityEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
