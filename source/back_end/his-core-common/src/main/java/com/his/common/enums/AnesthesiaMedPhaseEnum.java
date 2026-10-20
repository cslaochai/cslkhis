package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉用药阶段
 */
@Getter
public enum AnesthesiaMedPhaseEnum {

    INDUCTION(1, "诱导"),
    MAINTENANCE(2, "维持"),
    EMERGENCE(3, "苏醒");

    private final Integer code;
    private final String label;

    AnesthesiaMedPhaseEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AnesthesiaMedPhaseEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaMedPhaseEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AnesthesiaMedPhaseEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaMedPhaseEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaMedPhaseEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
