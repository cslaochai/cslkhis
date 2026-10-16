package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE发生时机枚举
 */
@Getter
public enum VteOnsetEnum {

    IN_HOSPITAL(1, "院内发生"),
    PRE_EXISTING(2, "入院时已存在");

    private final int code;
    private final String label;

    VteOnsetEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteOnsetEnum fromCode(int code) {
        for (VteOnsetEnum item : values()) {
            if (item.code == code) {
                return item;
            }
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
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        VteOnsetEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        VteOnsetEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
