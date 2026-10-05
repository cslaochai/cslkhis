package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：检验危急值偏离方向文案（码值口径 = 库列注释：1-偏低 2-偏高；
 * 与医技模块 CriticalTypeEnum 同码同义，因 his-report 不依赖 his-medicaltech 此处单列）。
 */
@Getter
public enum CdrCriticalTypeEnum {

    LOW(1, "偏低"),
    HIGH(2, "偏高");

    private final int code;
    private final String label;

    CdrCriticalTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrCriticalTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrCriticalTypeEnum e : values()) {
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

    /** 展示用：null / 脏码值一律返回空串，绝不返回 null、绝不回落合法值 */
    public static String getText(Integer code) {
        CdrCriticalTypeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
