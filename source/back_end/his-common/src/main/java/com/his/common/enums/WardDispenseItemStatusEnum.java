package com.his.common.enums;

import lombok.Getter;

/**
 * 住院摆药明细状态枚举（待配药 / 已配药 / 已核对 / 已退药）。
 * 与发药单口径（待发药/已发药/已退药/已取消）是两条不同列，不可混用。
 */
@Getter
public enum WardDispenseItemStatusEnum {

    PENDING(1, "待配药"),
    DISPENSED(2, "已配药"),
    CHECKED(3, "已核对"),
    RETURNED(4, "已退药");

    private final int code;
    private final String label;

    WardDispenseItemStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static WardDispenseItemStatusEnum fromCode(int code) {
        for (WardDispenseItemStatusEnum item : values()) {
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
        WardDispenseItemStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        WardDispenseItemStatusEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
