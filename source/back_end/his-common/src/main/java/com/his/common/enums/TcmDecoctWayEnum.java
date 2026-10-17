package com.his.common.enums;

import lombok.Getter;

/**
 * 中药煎服方式枚举（1-代煎 2-自煎）。
 * <p>
 * 与煎药任务状态（待煎/已煎/已取/已作废）是两个不同口径：本枚举说的是「由谁煎」，
 * 那个说的是「煎到哪一步」，码值不可互相套用。
 */
@Getter
public enum TcmDecoctWayEnum {

    BY_HOSPITAL(1, "代煎"),
    SELF(2, "自煎");

    private final int code;
    private final String label;

    TcmDecoctWayEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TcmDecoctWayEnum fromCode(int code) {
        for (TcmDecoctWayEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        TcmDecoctWayEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        TcmDecoctWayEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
