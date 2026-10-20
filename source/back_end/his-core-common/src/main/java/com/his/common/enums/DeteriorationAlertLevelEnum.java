package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 恶化预警级（由体征评分现算，不落库）
 */
@Getter
public enum DeteriorationAlertLevelEnum {

    NOT_TRIGGERED(0, "未触发"),
    WATCH(1, "关注"),
    HIGH_RISK(2, "高危");

    private final Integer code;
    private final String label;

    DeteriorationAlertLevelEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DeteriorationAlertLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeteriorationAlertLevelEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DeteriorationAlertLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeteriorationAlertLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeteriorationAlertLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
