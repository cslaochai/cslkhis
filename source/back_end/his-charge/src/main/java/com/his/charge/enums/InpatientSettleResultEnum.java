package com.his.charge.enums;

import lombok.Getter;

/**
 * 住院结算结果文案（派生值：由账单应缴与已收金额比对得出，不是库里的一列；
 * 账单本身的支付状态见 CdrInpatientSettleStatusEnum / his-charge 账单枚举）。
 */
@Getter
public enum InpatientSettleResultEnum {

    SETTLED(1, "已结清"),
    ARREAR(2, "欠费");

    private final int code;
    private final String label;

    InpatientSettleResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientSettleResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InpatientSettleResultEnum e : values()) {
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
        InpatientSettleResultEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        InpatientSettleResultEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
