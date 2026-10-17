package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 收治指令状态枚举
 */
@Getter
public enum AdmissionOrderStatusEnum {

    PENDING(1, "待收治"),
    ADMITTED(2, "已收治"),
    VOIDED(3, "已作废"),
    EXPIRED(4, "已过期");

    private final int code;
    private final String label;

    AdmissionOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdmissionOrderStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AdmissionOrderStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AdmissionOrderStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AdmissionOrderStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AdmissionOrderStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
