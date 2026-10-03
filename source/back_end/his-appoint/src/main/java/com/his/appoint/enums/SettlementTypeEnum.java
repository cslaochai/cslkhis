package com.his.appoint.enums;

import lombok.Getter;

/**
 * 结算方式枚举
 */
@Getter
public enum SettlementTypeEnum {

    SELF_PAY(1, "自费"),
    URBAN_EMPLOYEE(2, "城镇职工医保"),
    URBAN_RESIDENT(3, "城乡居民医保"),
    PUBLIC(4, "公费"),
    COMMERCIAL_INSURANCE(5, "商业保险");

    private final int code;
    private final String label;

    SettlementTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SettlementTypeEnum fromCode(int code) {
        for (SettlementTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }
}
