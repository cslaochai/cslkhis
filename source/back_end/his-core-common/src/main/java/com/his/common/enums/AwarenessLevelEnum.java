package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AwarenessLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AwarenessLevelEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AwarenessLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AwarenessLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AwarenessLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
