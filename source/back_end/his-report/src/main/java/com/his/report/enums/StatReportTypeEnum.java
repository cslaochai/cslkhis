package com.his.report.enums;

import lombok.Getter;

/**
 * 卫生统计报表类型文案（码值口径 = 库列注释）。
 */
@Getter
public enum StatReportTypeEnum {

    HEALTH_STAT_ANNUAL(1, "卫统年报"),
    DISCHARGE_MONTHLY(2, "出院患者统计月报"),
    OPERATION_WORKLOAD(3, "手术工作量专项报表");

    private final int code;
    private final String label;

    StatReportTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StatReportTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatReportTypeEnum e : values()) {
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
        StatReportTypeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        return code == null ? "未知类型" : "未知类型(" + code + ")";
    }
}
