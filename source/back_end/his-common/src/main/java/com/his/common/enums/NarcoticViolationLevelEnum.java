package com.his.common.enums;

import lombok.Getter;

/**
 * 麻精处方违规级别（BLOCK-拦截 WARN-警告）
 */
@Getter
public enum NarcoticViolationLevelEnum {

    BLOCK("BLOCK", "拦截"),
    WARN("WARN", "警告");

    private final String code;
    private final String label;

    NarcoticViolationLevelEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NarcoticViolationLevelEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (NarcoticViolationLevelEnum item : values()) {
            if (item.code.equals(code)) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(String code) {
        NarcoticViolationLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(String code) {
        NarcoticViolationLevelEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
