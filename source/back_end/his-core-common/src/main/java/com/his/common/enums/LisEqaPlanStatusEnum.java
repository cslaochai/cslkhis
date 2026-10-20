package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 室间质评批次状态枚举（1-待收样 2-检测中 3-已上报 4-已回报 5-已归档）
 */
@Getter
public enum LisEqaPlanStatusEnum {

    WAITING_SAMPLE(1, "待收样"),
    TESTING(2, "检测中"),
    REPORTED(3, "已上报"),
    RETURNED(4, "已回报"),
    ARCHIVED(5, "已归档");

    private final int code;
    private final String label;

    LisEqaPlanStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static LisEqaPlanStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (LisEqaPlanStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        LisEqaPlanStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        LisEqaPlanStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        LisEqaPlanStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
