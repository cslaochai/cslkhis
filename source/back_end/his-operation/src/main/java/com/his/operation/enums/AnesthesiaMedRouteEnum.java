package com.his.operation.enums;

import lombok.Getter;

/**
 * 麻醉给药途径
 */
@Getter
public enum AnesthesiaMedRouteEnum {

    IV_PUSH(1, "静脉推注"),
    IV_PUMP(2, "静脉泵注"),
    IV_DRIP(3, "静脉滴注"),
    INHALATION(4, "吸入"),
    INTRAMUSCULAR(5, "肌注"),
    INTRASPINAL(6, "椎管内"),
    LOCAL_INFILTRATION(7, "局麻浸润"),
    OTHER(8, "其他");

    private final Integer code;
    private final String label;

    AnesthesiaMedRouteEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AnesthesiaMedRouteEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AnesthesiaMedRouteEnum item : values()) {
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
        AnesthesiaMedRouteEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        AnesthesiaMedRouteEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
