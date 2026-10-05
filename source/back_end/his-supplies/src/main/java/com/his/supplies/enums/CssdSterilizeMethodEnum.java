package com.his.supplies.enums;

import lombok.Getter;

/**
 * CSSD 灭菌方式枚举（码值口径 = biz_cssd_pack.sterilize_method 列注释）。
 */
@Getter
public enum CssdSterilizeMethodEnum {

    STEAM(1, "高压蒸汽"),
    ETO(2, "环氧乙烷"),
    PLASMA(3, "低温等离子");

    private final int code;
    private final String label;

    CssdSterilizeMethodEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CssdSterilizeMethodEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CssdSterilizeMethodEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        CssdSterilizeMethodEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }
}
