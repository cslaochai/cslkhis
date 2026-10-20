package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 慢病档案认定状态枚举
 */
@Getter
public enum ChronicConfirmStatusEnum {

    CANCELLED(2, "已取消"),
    CONFIRMED(1, "已认定"),
    PENDING(0, "待认定");

    private final int code;
    private final String label;

    ChronicConfirmStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ChronicConfirmStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ChronicConfirmStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ChronicConfirmStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ChronicConfirmStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ChronicConfirmStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
