package com.his.common.enums;

import lombok.Getter;

/**
 * 室间质评批次状态枚举（1-待收样 2-检测中 3-已上报 4-已回报 5-已归档）
 */
@Getter
public enum LisEqaPlanStatusEnum {

    WAITING_SAMPLE(1, "待收样"),
    TESTING(2, "检测中"),
    REPORTED(3, "已上报"),
    RETURNED(4, "已回报"),
    ARCHIVED(5, "已归档");

    private final int code;
    private final String label;

    LisEqaPlanStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static LisEqaPlanStatusEnum fromCode(int code) {
        for (LisEqaPlanStatusEnum status : values()) {
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
        LisEqaPlanStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        LisEqaPlanStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
