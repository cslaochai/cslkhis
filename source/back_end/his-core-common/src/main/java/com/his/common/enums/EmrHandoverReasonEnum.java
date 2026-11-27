package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历草稿移交原因（医生离岗/调离导致名下草稿需他人代补）
 */
@Getter
public enum EmrHandoverReasonEnum {

    RESIGN(1, "离职"),
    LEAVE(2, "长期休假"),
    TRANSFER(3, "调离临床岗位"),
    OTHER(4, "其他");

    private final int code;
    private final String label;

    EmrHandoverReasonEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EmrHandoverReasonEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EmrHandoverReasonEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        EmrHandoverReasonEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EmrHandoverReasonEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EmrHandoverReasonEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
