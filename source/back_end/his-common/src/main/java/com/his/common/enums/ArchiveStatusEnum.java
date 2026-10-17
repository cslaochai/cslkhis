package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病案归档状态枚举
 */
@Getter
public enum ArchiveStatusEnum {

    PENDING(1, "待归档"),
    ARCHIVED(2, "已归档"),
    SEALED(3, "已封存");

    private final int code;
    private final String label;

    ArchiveStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ArchiveStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ArchiveStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ArchiveStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ArchiveStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ArchiveStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
