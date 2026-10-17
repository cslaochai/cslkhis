package com.his.common.enums;

import lombok.Getter;

/**
 * 室间质评判定口径枚举（0-无法判定 1-SDI 2-允许总误差 3-可接受范围）
 */
@Getter
public enum EqaJudgeModeEnum {

    NONE(0, "无法判定"),
    SDI(1, "SDI"),
    TEA(2, "允许总误差"),
    RANGE(3, "可接受范围");

    private final int code;
    private final String label;

    EqaJudgeModeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EqaJudgeModeEnum fromCode(int code) {
        for (EqaJudgeModeEnum mode : values()) {
            if (mode.code == code) return mode;
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
        EqaJudgeModeEnum mode = code == null ? null : fromCode(code);
        return mode == null ? "" : mode.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        EqaJudgeModeEnum mode = code == null ? null : fromCode(code);
        if (mode != null) {
            return mode.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
