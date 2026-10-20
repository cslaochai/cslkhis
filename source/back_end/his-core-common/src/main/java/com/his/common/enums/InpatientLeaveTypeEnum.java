package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院请假类别枚举
 */
@Getter
public enum InpatientLeaveTypeEnum {

    DAY_TRIP(1, "临时外出当日往返"),
    OVERNIGHT(2, "离院过夜"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    InpatientLeaveTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static InpatientLeaveTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InpatientLeaveTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InpatientLeaveTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InpatientLeaveTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InpatientLeaveTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
