package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历质控类型枚举
 */
@Getter
public enum RecordQcTypeEnum {

    COMPREHENSIVE(0, "综合"),
    COMPLETENESS(1, "完整性检查"),
    NORMATIVE(2, "规范性检查"),
    LOGIC(3, "逻辑性检查"),
    AI_INHERENT(4, "AI内涵质控");

    private final int code;
    private final String label;

    RecordQcTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RecordQcTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RecordQcTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RecordQcTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RecordQcTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
