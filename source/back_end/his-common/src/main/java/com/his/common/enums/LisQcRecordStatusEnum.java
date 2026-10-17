package com.his.common.enums;

import lombok.Getter;

/**
 * 室内质控记录状态枚举（0-未判定 1-在控 2-警告 3-失控）
 */
@Getter
public enum LisQcRecordStatusEnum {

    UNJUDGED(0, "未判定"),
    IN_CONTROL(1, "在控"),
    WARNING(2, "警告"),
    OUT_OF_CONTROL(3, "失控");

    private final int code;
    private final String label;

    LisQcRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static LisQcRecordStatusEnum fromCode(int code) {
        for (LisQcRecordStatusEnum status : values()) {
            if (status.code == code) return status;
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
        LisQcRecordStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        LisQcRecordStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
