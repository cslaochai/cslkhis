package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 计费状态枚举
 */
@Getter
public enum BillingStatusEnum {

    UNBILLED(0, "未计费"),
    BILLED(1, "已计费"),
    FAILED(2, "计费失败"),
    NO_NEED(3, "无需计费");

    private final int code;
    private final String label;

    BillingStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BillingStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BillingStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BillingStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BillingStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BillingStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
