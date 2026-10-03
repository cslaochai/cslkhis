package com.his.common.enums;

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

    public static SettlementModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SettlementModeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
