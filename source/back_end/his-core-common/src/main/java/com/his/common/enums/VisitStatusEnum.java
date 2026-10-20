package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 就诊次状态枚举
 */
@Getter
public enum VisitStatusEnum {

    CANCELLED(0, "已取消"),
    OPEN(1, "进行中"),
    FINISHED(2, "已完成");

    private final int code;
    private final String label;

    VisitStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VisitStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (VisitStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        VisitStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        VisitStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        VisitStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
