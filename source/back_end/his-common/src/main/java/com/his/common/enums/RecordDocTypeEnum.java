package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历文书操作日志的单据类型枚举
 */
@Getter
public enum RecordDocTypeEnum {

    MEDICAL(1, "住院病历文书"),
    NURSING(2, "护理文书");

    private final int code;
    private final String label;

    RecordDocTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordDocTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RecordDocTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RecordDocTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RecordDocTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RecordDocTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
