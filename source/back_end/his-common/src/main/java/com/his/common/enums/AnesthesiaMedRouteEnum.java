package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AnesthesiaMedRouteEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaMedRouteEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AnesthesiaMedRouteEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaMedRouteEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaMedRouteEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
