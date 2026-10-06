package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：住院结算账单状态文案（码值口径 = 库列注释；
 * 出院结算节点翻译的是账单状态，不是"结清/欠费"这种派生值）。
 */
@Getter
public enum CdrInpatientSettleStatusEnum {

    UNPAID(1, "待支付"),
    PART_PAID(2, "部分支付"),
    PAID(3, "已支付"),
    VOIDED(4, "已作废"),
    REFUNDED(5, "已退费");

    private final int code;
    private final String label;

    CdrInpatientSettleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrInpatientSettleStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrInpatientSettleStatusEnum e : values()) {
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
        CdrInpatientSettleStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        CdrInpatientSettleStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
