package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 治疗执行状态枚举
 */
@Getter
public enum TreatmentExecStatusEnum {

    PENDING(0, "待执行"),
    DONE(1, "已执行"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String label;

    TreatmentExecStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TreatmentExecStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TreatmentExecStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TreatmentExecStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TreatmentExecStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TreatmentExecStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
