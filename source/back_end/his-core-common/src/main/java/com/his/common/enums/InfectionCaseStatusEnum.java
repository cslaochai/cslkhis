package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 院感病例核实状态枚举
 */
@Getter
public enum InfectionCaseStatusEnum {

    PENDING(1, "待核实"),
    CONFIRMED(2, "已确认"),
    EXCLUDED(3, "已排除");

    private final int code;
    private final String label;

    InfectionCaseStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectionCaseStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InfectionCaseStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InfectionCaseStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InfectionCaseStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InfectionCaseStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
