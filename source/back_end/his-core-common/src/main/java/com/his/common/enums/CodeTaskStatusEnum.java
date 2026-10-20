package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病案编码任务状态枚举
 */
@Getter
public enum CodeTaskStatusEnum {

    PENDING(1, "待编码"),
    SUBMITTED(2, "已提交"),
    DONE(3, "已完成"),
    REWORK(4, "已退修");

    private final int code;
    private final String label;

    CodeTaskStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CodeTaskStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CodeTaskStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CodeTaskStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CodeTaskStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CodeTaskStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
