package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 预防措施落实状态枚举
 */
@Getter
public enum VtePreventStatusEnum {

    PENDING(0, "待落实"),
    DONE(1, "已落实"),
    CONTRAINDICATION(2, "禁忌未用"),
    REFUSED(3, "患者拒绝");

    private final int code;
    private final String label;

    VtePreventStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VtePreventStatusEnum fromCode(int code) {
        for (VtePreventStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String labelOf(Integer code) {
        VtePreventStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        VtePreventStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
