package com.his.pharmacy.enums;

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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        CssdSterilizeMethodEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        CssdSterilizeMethodEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
