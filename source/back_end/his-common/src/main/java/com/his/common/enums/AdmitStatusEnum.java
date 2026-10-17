package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 在院状态枚举
 */
@Getter
public enum AdmitStatusEnum {

    DISCHARGED(0, "已出院"),
    IN_HOSPITAL(1, "在院");

    private final int code;
    private final String label;

    AdmitStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdmitStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AdmitStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AdmitStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AdmitStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AdmitStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
