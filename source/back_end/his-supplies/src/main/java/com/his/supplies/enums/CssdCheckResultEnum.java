package com.his.supplies.enums;

import lombok.Getter;

/**
 * CSSD 处置结果枚举（码值口径 = biz_cssd_trace.result 列注释）。
 */
@Getter
public enum CssdCheckResultEnum {

    OK(1, "合格"),
    NG(2, "不合格");

    private final int code;
    private final String label;

    CssdCheckResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CssdCheckResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CssdCheckResultEnum e : values()) {
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

    /** 未知码值渲染「未知(码值)」，绝不回落成合法值 */
    public static String getText(Integer code) {
        CssdCheckResultEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        CssdCheckResultEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
