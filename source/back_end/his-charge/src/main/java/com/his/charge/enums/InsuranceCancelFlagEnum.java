package com.his.charge.enums;

import lombok.Getter;

/**
 * 退费是否已撤销医保报盘（码值口径见该列注释）。
 */
@Getter
public enum InsuranceCancelFlagEnum {

    NOT_INVOLVED(0, "不涉及"),
    CANCEL_SENT(1, "已发撤销报盘");

    private final Integer code;
    private final String desc;

    InsuranceCancelFlagEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InsuranceCancelFlagEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InsuranceCancelFlagEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        InsuranceCancelFlagEnum item = fromCode(code);
        return item == null ? "" : item.desc;
    }

    public static String labelOrUnknown(Integer code) {
        InsuranceCancelFlagEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
