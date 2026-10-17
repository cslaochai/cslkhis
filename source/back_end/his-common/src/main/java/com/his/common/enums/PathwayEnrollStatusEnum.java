package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 临床路径入径状态枚举
 */
@Getter
public enum PathwayEnrollStatusEnum {

    ENROLLED(1, "在径"),
    FINISHED(2, "已完成"),
    ABORTED(3, "已退径");

    private final int code;
    private final String label;

    PathwayEnrollStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PathwayEnrollStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PathwayEnrollStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PathwayEnrollStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PathwayEnrollStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PathwayEnrollStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
