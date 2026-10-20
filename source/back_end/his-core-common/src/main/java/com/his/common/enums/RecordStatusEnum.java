package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历状态枚举
 */
@Getter
public enum RecordStatusEnum {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    ARCHIVED(3, "已归档"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    RecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RecordStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RecordStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RecordStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
