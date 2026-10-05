package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 血袋状态枚举（血袋自身流转：待配血 → 已配血 → 已发血 → 已输注）。
 */
@Getter
public enum BloodBagStatusEnum {

    PENDING(0, "待配血"),
    CROSSMATCHED(1, "已配血"),
    ISSUED(2, "已发血"),
    INFUSED(3, "已输注");

    private final int code;
    private final String label;

    BloodBagStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BloodBagStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BloodBagStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null 返回「—」；脏值返回空串。 */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        BloodBagStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        BloodBagStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
