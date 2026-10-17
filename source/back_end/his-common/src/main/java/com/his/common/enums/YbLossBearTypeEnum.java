package com.his.common.enums;

import lombok.Getter;

/**
 * 医保扣款损失承担方式枚举（1-院方承担 2-科室承担 3-个人承担 4-科室+个人共担）
 */
@Getter
public enum YbLossBearTypeEnum {

    HOSPITAL(1, "院方承担"),
    DEPT(2, "科室承担"),
    EMPLOYEE(3, "个人承担"),
    DEPT_EMPLOYEE(4, "科室+个人共担");

    private final int code;
    private final String label;

    YbLossBearTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static YbLossBearTypeEnum fromCode(int code) {
        for (YbLossBearTypeEnum type : values()) {
            if (type.code == code) return type;
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
        YbLossBearTypeEnum type = code == null ? null : fromCode(code);
        return type == null ? "" : type.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        YbLossBearTypeEnum type = code == null ? null : fromCode(code);
        if (type != null) {
            return type.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
