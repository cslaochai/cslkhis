package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 预防措施类别枚举
 */
@Getter
public enum VteMeasureTypeEnum {

    BASIC(1, "基础预防"),
    PHYSICAL(2, "物理预防"),
    DRUG(3, "药物预防");

    private final int code;
    private final String label;

    VteMeasureTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteMeasureTypeEnum fromCode(int code) {
        for (VteMeasureTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        VteMeasureTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        VteMeasureTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
