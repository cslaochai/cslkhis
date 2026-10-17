package com.his.common.enums;

import lombok.Getter;

/**
 * 校验/评估结论枚举（0-不通过 1-通过），全仓「通过 / 不通过」这一档判定的唯一口径
 */
@Getter
public enum CheckResultEnum {

    FAIL(0, "不通过"),
    PASS(1, "通过");

    private final int code;
    private final String label;

    CheckResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CheckResultEnum fromCode(int code) {
        for (CheckResultEnum item : values()) {
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
        CheckResultEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        CheckResultEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
