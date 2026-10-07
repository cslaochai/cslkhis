package com.his.common.enums;

import lombok.Getter;

/**
 * 是否标志枚举（1-是 0-否），所有「是否 xxx」的 0/1 标志位共用
 */
@Getter
public enum YesOrNoEnum {

    YES(1, "是"),
    NO(0, "否");

    private final int code;
    private final String label;

    YesOrNoEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static YesOrNoEnum fromCode(int code) {
        for (YesOrNoEnum flag : values()) {
            if (flag.code == code) return flag;
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
        YesOrNoEnum flag = code == null ? null : fromCode(code);
        return flag == null ? "" : flag.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        YesOrNoEnum flag = code == null ? null : fromCode(code);
        if (flag != null) {
            return flag.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
