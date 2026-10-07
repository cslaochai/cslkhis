package com.his.operation.enums;

import lombok.Getter;

/**
 * PACU 清醒程度
 */
@Getter
public enum AwarenessLevelEnum {

    FULLY_AWAKE(1, "完全清醒"),
    SLEEPY_AROUSABLE(2, "嗜睡可唤醒"),
    NOT_AWAKE(3, "未清醒");

    private final Integer code;
    private final String label;

    AwarenessLevelEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AwarenessLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AwarenessLevelEnum item : values()) {
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
        AwarenessLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        AwarenessLevelEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
