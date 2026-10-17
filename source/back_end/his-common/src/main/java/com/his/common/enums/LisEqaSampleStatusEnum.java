package com.his.common.enums;

import lombok.Getter;

/**
 * 室间质评盲样流转状态枚举（0-待检测 1-已检测 2-已上报 3-已回报）
 */
@Getter
public enum LisEqaSampleStatusEnum {

    PENDING_TEST(0, "待检测"),
    TESTED(1, "已检测"),
    REPORTED(2, "已上报"),
    RETURNED(3, "已回报");

    private final int code;
    private final String label;

    LisEqaSampleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static LisEqaSampleStatusEnum fromCode(int code) {
        for (LisEqaSampleStatusEnum status : values()) {
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
        LisEqaSampleStatusEnum status = code == null ? null : fromCode(code);
        return status == null ? "" : status.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        LisEqaSampleStatusEnum status = code == null ? null : fromCode(code);
        if (status != null) {
            return status.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
