package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrDischargeStatusEnum {
    NORMAL(1, "正常出院"),
    TRANSFER(2, "转科"),
    SELF_DISCHARGE(3, "自动出院");

    private final int code;
    private final String label;

    CdrDischargeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrDischargeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrDischargeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrDischargeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrDischargeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrDischargeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
