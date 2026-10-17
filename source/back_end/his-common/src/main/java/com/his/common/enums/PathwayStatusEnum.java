package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 临床路径状态枚举
 */
@Getter
public enum PathwayStatusEnum {

    DRAFT(1, "草稿"),
    ACTIVE(2, "使用中"),
    DEPRECATED(3, "已停用");

    private final int code;
    private final String label;

    PathwayStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PathwayStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PathwayStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PathwayStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PathwayStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PathwayStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
