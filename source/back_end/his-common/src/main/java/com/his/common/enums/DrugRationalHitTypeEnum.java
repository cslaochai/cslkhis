package com.his.common.enums;

import lombok.Getter;

/**
 * 合理用药审查命中类型枚举（字符串码值，出参契约的命中类型列）
 */
@Getter
public enum DrugRationalHitTypeEnum {

    INTERACTION("INTERACTION", "相互作用"),
    DOSE_SINGLE("DOSE_SINGLE", "单次超量"),
    DOSE_DAILY("DOSE_DAILY", "日累计超量");

    private final String code;
    private final String label;

    DrugRationalHitTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DrugRationalHitTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (DrugRationalHitTypeEnum type : values()) {
            if (type.code.equals(code)) return type;
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
        DrugRationalHitTypeEnum type = fromCode(code);
        return type == null ? "" : type.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(String code) {
        DrugRationalHitTypeEnum type = fromCode(code);
        if (type != null) {
            return type.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
