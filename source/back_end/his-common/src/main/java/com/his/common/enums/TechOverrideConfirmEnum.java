package com.his.common.enums;

import lombok.Getter;

/**
 * 技术授权越权登记确认状态枚举（1-待上级确认 2-已确认）
 */
@Getter
public enum TechOverrideConfirmEnum {

    PENDING(1, "待上级确认"),
    CONFIRMED(2, "已确认");

    private final int code;
    private final String label;

    TechOverrideConfirmEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TechOverrideConfirmEnum fromCode(int code) {
        for (TechOverrideConfirmEnum status : values()) {
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
        TechOverrideConfirmEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        TechOverrideConfirmEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
