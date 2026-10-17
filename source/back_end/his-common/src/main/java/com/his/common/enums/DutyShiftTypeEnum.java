package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 值守班段枚举（字典 his_duty_shift）
 */
@Getter
public enum DutyShiftTypeEnum {

    /**
     * 白段：白天接管全院应急协调
     */
    DAY(1, "白班"),
    /**
     * 夜段：跨零点，归开始日
     */
    NIGHT(2, "夜班");

    private final int code;
    private final String label;

    DutyShiftTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DutyShiftTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DutyShiftTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DutyShiftTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DutyShiftTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DutyShiftTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
