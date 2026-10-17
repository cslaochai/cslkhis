package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历质控动作发生级枚举
 */
@Getter
public enum RecordQcLevelEnum {

    DEPT(1, "科级"),
    ARCHIVE(2, "病案室"),
    MEDAFFAIRS(3, "医务处");

    private final int code;
    private final String label;

    RecordQcLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RecordQcLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RecordQcLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RecordQcLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RecordQcLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
