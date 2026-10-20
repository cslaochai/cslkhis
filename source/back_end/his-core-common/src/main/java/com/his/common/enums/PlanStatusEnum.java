package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 膳食方案状态枚举
 */
@Getter
public enum PlanStatusEnum {

    RUNNING(1, "执行中"),
    STOPPED(2, "已停止"),
    CANCELED(3, "已作废");

    private final int code;
    private final String label;

    PlanStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PlanStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PlanStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PlanStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PlanStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PlanStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
