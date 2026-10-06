package com.his.patient.enums;

import lombok.Getter;

/**
 * 死亡地点枚举
 */
@Getter
public enum DeathPlaceEnum {

    HOSPITAL(1, "医院"),
    TRANSFER(2, "来院途中"),
    HOME(3, "家中"),
    CIVIL_AGENCY(4, "民政管理机构"),
    OTHER_INSTITUTION(5, "其他机构"),
    UNSPECIFIED(9, "未指明");

    private final int code;
    private final String label;

    DeathPlaceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeathPlaceEnum fromCode(int code) {
        for (DeathPlaceEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        DeathPlaceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        DeathPlaceEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
