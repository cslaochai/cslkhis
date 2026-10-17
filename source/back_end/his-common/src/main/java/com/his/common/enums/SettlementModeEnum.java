package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 结算方式枚举
 */
public enum SettlementModeEnum {

    SELF_PAY(1, "自费"),
    INSURANCE(2, "医保");

    private final Integer code;
    private final String desc;

    SettlementModeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SettlementModeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SettlementModeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        SettlementModeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SettlementModeEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        SettlementModeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
