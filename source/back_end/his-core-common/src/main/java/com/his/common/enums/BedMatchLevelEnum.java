package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 床位匹配等级（按申请科室与床位类型现算，不落库）
 */
@Getter
public enum BedMatchLevelEnum {

    SAME_DEPT_SAME_TYPE(1, "同科同型"),
    SAME_DEPT_OTHER_TYPE(2, "同科异型"),
    CROSS_DEPT_SAME_TYPE(3, "跨科同型"),
    CROSS_DEPT_OTHER_TYPE(4, "跨科异型");

    private final Integer code;
    private final String label;

    BedMatchLevelEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static BedMatchLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BedMatchLevelEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BedMatchLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BedMatchLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BedMatchLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
