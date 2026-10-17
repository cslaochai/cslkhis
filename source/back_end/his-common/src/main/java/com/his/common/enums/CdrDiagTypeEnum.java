package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrDiagTypeEnum {
    MAIN(1, "主要诊断"),
    OTHER(2, "其他诊断");

    private final int code;
    private final String label;

    CdrDiagTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrDiagTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrDiagTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrDiagTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrDiagTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrDiagTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
