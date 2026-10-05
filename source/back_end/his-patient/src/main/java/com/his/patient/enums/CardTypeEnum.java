package com.his.patient.enums;

import lombok.Getter;

/**
 * 证件类型枚举（1-身份证 2-护照 3-军官证）
 */
@Getter
public enum CardTypeEnum {

    ID_CARD(1, "身份证"),
    PASSPORT(2, "护照"),
    OFFICER_CARD(3, "军官证");

    private final int code;
    private final String label;

    CardTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CardTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CardTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "";
        }
        CardTypeEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        CardTypeEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
