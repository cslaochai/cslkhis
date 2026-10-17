package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 技术授权越权登记确认状态枚举（1-待上级确认 2-已确认）
 */
@Getter
public enum TechOverrideConfirmEnum {

    PENDING(1, "待上级确认"),
    CONFIRMED(2, "已确认");

    private final int code;
    private final String label;

    TechOverrideConfirmEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static TechOverrideConfirmEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TechOverrideConfirmEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TechOverrideConfirmEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TechOverrideConfirmEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TechOverrideConfirmEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
