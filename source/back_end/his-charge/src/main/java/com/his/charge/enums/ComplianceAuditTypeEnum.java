package com.his.charge.enums;

import lombok.Getter;

/**
 * 医保合规审核的发起方式文案（码值口径 = 库列注释：1-结算前自查 2-批量筛查 3-医保反馈复核）。
 */
@Getter
public enum ComplianceAuditTypeEnum {

    PRE_SETTLE_SELF_CHECK(1, "结算前自查"),
    BATCH_SCREENING(2, "批量筛查"),
    INSURANCE_FEEDBACK(3, "医保反馈复核");

    private final int code;
    private final String label;

    ComplianceAuditTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ComplianceAuditTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ComplianceAuditTypeEnum e : values()) {
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
        ComplianceAuditTypeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        ComplianceAuditTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
