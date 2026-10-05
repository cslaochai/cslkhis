package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 诊断依据枚举
 */
@Getter
public enum VteDiagnosisBasisEnum {

    ULTRASOUND(1, "超声"),
    CT_PA(2, "CT 肺动脉造影"),
    VENOGRAPHY(3, "静脉造影"),
    CLINICAL(4, "临床诊断"),
    OTHER(5, "其他");

    private final int code;
    private final String label;

    VteDiagnosisBasisEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteDiagnosisBasisEnum fromCode(int code) {
        for (VteDiagnosisBasisEnum item : values()) {
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
        VteDiagnosisBasisEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        VteDiagnosisBasisEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
