package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static NpoStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NpoStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NpoStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NpoStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        NpoStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
