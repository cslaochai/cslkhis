package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrChargeTypeEnum {
    REGISTRATION(1, "挂号费"),
    DRUG(2, "药品费"),
    EXAM(3, "检查费"),
    LAB(4, "检验费"),
    TREATMENT(5, "治疗费"),
    GENERAL(6, "综合收费");

    private final int code;
    private final String label;

    CdrChargeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrChargeTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrChargeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrChargeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrChargeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrChargeTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
