package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历草稿移交代补状态
 */
@Getter
public enum EmrHandoverStatusEnum {

    PENDING(1, "待代补"),
    COMPLETED(2, "已完成"),
    REVOKED(3, "已撤销");

    private final int code;
    private final String label;

    EmrHandoverStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EmrHandoverStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (EmrHandoverStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        EmrHandoverStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        EmrHandoverStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        EmrHandoverStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
