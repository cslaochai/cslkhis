package com.his.appoint.enums;

import lombok.Getter;

/**
 * 门诊分诊等级（区别于急诊 I~IV 级）。
 */
@Getter
public enum TriageLevelEnum {

    CRITICAL(1, "1级·危重"),
    EMERGENCY(2, "2级·急症"),
    URGENT(3, "3级·亚急"),
    NON_URGENT(4, "4级·非急");

    private final Integer code;
    private final String desc;

    TriageLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TriageLevelEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (TriageLevelEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return parse(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        TriageLevelEnum item = parse(code);
        return item != null ? item.desc : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        TriageLevelEnum item = parse(code);
        return item != null ? item.desc : "未知(" + code + ")";
    }
}
