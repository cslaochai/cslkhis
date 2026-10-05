package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：住院文书类型文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrInpatientRecordTypeEnum {

    ADMISSION(1, "入院记录"),
    FIRST_PROGRESS(2, "首次病程"),
    DAILY_PROGRESS(3, "日常病程"),
    PRE_OP_SUMMARY(4, "术前小结"),
    OPERATION(5, "手术记录"),
    POST_OP_FIRST_PROGRESS(6, "术后首次病程"),
    DISCHARGE(7, "出院记录"),
    DEATH(8, "死亡记录"),
    CONSULTATION(9, "会诊记录"),
    TRANSFER(10, "转科记录"),
    TRANSFUSION(11, "输血记录");

    private final int code;
    private final String label;

    CdrInpatientRecordTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrInpatientRecordTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrInpatientRecordTypeEnum e : values()) {
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
        CdrInpatientRecordTypeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
