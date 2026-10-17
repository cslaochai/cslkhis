package com.his.common.enums;

import lombok.Getter;

/**
 * 药品相互作用严重度枚举（1-禁忌 2-慎用；与字典 his_drug_interaction_severity 同码同义）
 */
@Getter
public enum DrugInteractionSeverityEnum {

    FORBIDDEN(1, "禁忌"),
    CAUTION(2, "慎用");

    private final int code;
    private final String label;

    DrugInteractionSeverityEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DrugInteractionSeverityEnum fromCode(int code) {
        for (DrugInteractionSeverityEnum severity : values()) {
            if (severity.code == code) return severity;
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
        DrugInteractionSeverityEnum severity = code == null ? null : fromCode(code);
        return severity == null ? "" : severity.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        DrugInteractionSeverityEnum severity = code == null ? null : fromCode(code);
        if (severity != null) {
            return severity.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
