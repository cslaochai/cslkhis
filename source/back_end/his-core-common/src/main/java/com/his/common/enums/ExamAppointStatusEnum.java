package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 检查预约状态枚举（1-已预约 2-已到检 3-已完成 4-已取消 5-爽约）
 */
@Getter
public enum ExamAppointStatusEnum {

    BOOKED(1, "已预约"),
    ARRIVED(2, "已到检"),
    FINISHED(3, "已完成"),
    CANCELLED(4, "已取消"),
    NOSHOW(5, "爽约");

    private final int code;
    private final String label;

    ExamAppointStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static ExamAppointStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ExamAppointStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ExamAppointStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ExamAppointStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ExamAppointStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
