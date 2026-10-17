package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 日间手术离院方式（1-按时离院 2-转普通住院 3-非计划再入院）
 */
@Getter
public enum DaySurgeryLeaveTypeEnum {

    NORMAL(1, "按时离院"),
    TRANSFER(2, "转普通住院"),
    READMIT(3, "非计划再入院");

    private final int code;
    private final String label;

    DaySurgeryLeaveTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DaySurgeryLeaveTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DaySurgeryLeaveTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DaySurgeryLeaveTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DaySurgeryLeaveTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DaySurgeryLeaveTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
