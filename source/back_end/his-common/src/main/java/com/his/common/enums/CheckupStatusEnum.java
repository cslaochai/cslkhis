package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 体检记录状态枚举
 */
@Getter
public enum CheckupStatusEnum {

    REGISTERED(1, "已登记"),
    IN_PROGRESS(2, "检查中"),
    FINISHED(3, "已完成"),
    REPORTED(4, "已出报告");

    private final int code;
    private final String label;

    CheckupStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CheckupStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CheckupStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CheckupStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CheckupStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CheckupStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
