package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院会诊状态枚举
 */
@Getter
public enum ConsultationStatusEnum {

    PENDING(0, "待应答"),
    FINISHED(1, "已完成"),
    CANCELLED(2, "已取消"),
    ACCEPTED(3, "已应答");

    private final int code;
    private final String label;

    ConsultationStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ConsultationStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ConsultationStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ConsultationStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ConsultationStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ConsultationStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
