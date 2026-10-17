package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 院感感染来源枚举
 */
@Getter
public enum InfectionSourceEnum {

    COMMUNITY(1, "社区感染"),
    HOSPITAL(2, "医院感染");

    private final int code;
    private final String label;

    InfectionSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectionSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InfectionSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InfectionSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InfectionSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InfectionSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
