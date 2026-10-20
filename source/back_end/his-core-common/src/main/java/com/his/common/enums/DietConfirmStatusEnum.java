package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 膳食方案接收状态枚举
 */
@Getter
public enum DietConfirmStatusEnum {

    PENDING(0, "待接收"),
    DONE(1, "已接收"),
    REJECTED(2, "已退回");

    private final int code;
    private final String label;

    DietConfirmStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietConfirmStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DietConfirmStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DietConfirmStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DietConfirmStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DietConfirmStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
