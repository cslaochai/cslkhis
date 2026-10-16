package com.his.operation.enums;

import lombok.Getter;

/**
 * 术前禁食禁饮状态
 */
@Getter
public enum NpoStatusEnum {

    NOT_FASTING(0, "未禁食"),
    FASTED_AS_REQUIRED(1, "已按要求禁食"),
    EMERGENCY_FULL_STOMACH(2, "急诊饱胃");

    private final Integer code;
    private final String label;

    NpoStatusEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NpoStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NpoStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        NpoStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        NpoStatusEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
