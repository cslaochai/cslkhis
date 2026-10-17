package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 室内质控记录状态枚举（0-未判定 1-在控 2-警告 3-失控）
 */
@Getter
public enum LisQcRecordStatusEnum {

    UNJUDGED(0, "未判定"),
    IN_CONTROL(1, "在控"),
    WARNING(2, "警告"),
    OUT_OF_CONTROL(3, "失控");

    private final int code;
    private final String label;

    LisQcRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static LisQcRecordStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (LisQcRecordStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        LisQcRecordStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        LisQcRecordStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        LisQcRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
