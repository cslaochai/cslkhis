package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * Rh 血型枚举
 */
@Getter
public enum RhTypeEnum {

    POSITIVE(1, "阳性"),
    NEGATIVE(2, "阴性");

    /**
     * 输血链 Rh 落库值：阳性
     */
    public static final String RH_POSITIVE = "阳";
    /**
     * 输血链 Rh 落库值：阴性
     */
    public static final String RH_NEGATIVE = "阴";
    private final int code;
    private final String label;

    RhTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RhTypeEnum fromCode(int code) {
        for (RhTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    // ── 输血链的 Rh 用中文单字存（阳 / 阴），不用 +/-：避免与「阴性/阳性」混用两套写法 ──

    /**
     * 码值不在枚举内（脏数据）返回空串，不回落到合法文案。
     */
    public static String getText(Integer code) {
        RhTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」）。
     */
    public static String labelOrUnknown(Integer code) {
        RhTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * Rh 归一：{@code + / 阳性 / 阳} → 阳；{@code - / 阴性 / 阴} → 阴；
     * 其余（null / 脏值）原样返回，不猜。
     */
    public static String normalizeRh(String rh) {
        if (rh == null) {
            return null;
        }
        String s = rh.trim();
        if (RH_POSITIVE.equals(s) || "阳性".equals(s) || "+".equals(s)) {
            return RH_POSITIVE;
        }
        if (RH_NEGATIVE.equals(s) || "阴性".equals(s) || "-".equals(s)) {
            return RH_NEGATIVE;
        }
        return s;
    }

    /**
     * 输血链 Rh 取值是否合法（落库前校验：归一后必须是阳/阴）
     */
    public static boolean isValidRh(String rh) {
        String s = normalizeRh(rh);
        return RH_POSITIVE.equals(s) || RH_NEGATIVE.equals(s);
    }

    /**
     * 输血链 Rh 展示文案（列表展示用，如「A 型 Rh(+)」由调用方拼装）
     */
    public static String getRhText(String rh) {
        String s = normalizeRh(rh);
        if (RH_NEGATIVE.equals(s)) {
            return "Rh(-)";
        }
        return RH_POSITIVE.equals(s) ? "Rh(+)" : "";
    }
}
