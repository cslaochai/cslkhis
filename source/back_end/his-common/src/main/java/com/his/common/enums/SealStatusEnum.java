package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历封存状态枚举
 */
@Getter
public enum SealStatusEnum {

    NONE(0, "未申请"),
    DONE(1, "已封存"),
    PENDING_ARCHIVE(2, "待归档后封存");

    private final int code;
    private final String label;

    SealStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SealStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SealStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SealStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SealStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        SealStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
