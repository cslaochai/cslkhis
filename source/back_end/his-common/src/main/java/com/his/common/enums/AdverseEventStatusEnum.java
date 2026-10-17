package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医疗不良事件处理状态枚举
 */
@Getter
public enum AdverseEventStatusEnum {

    REPORTED(1, "已上报待处理"),
    HANDLED(2, "处理中"),
    RECTIFIED(3, "已整改"),
    CLOSED(4, "已结案");

    private final int code;
    private final String label;

    AdverseEventStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdverseEventStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AdverseEventStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AdverseEventStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AdverseEventStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AdverseEventStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
