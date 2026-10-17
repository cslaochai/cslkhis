package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 审方动作枚举
 */
@Getter
public enum RxAuditActionEnum {

    PASS(1, "审方通过"),
    RETURN(2, "审方退回"),
    RESUBMIT(3, "退回后重提");

    private final int code;
    private final String label;

    RxAuditActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxAuditActionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RxAuditActionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RxAuditActionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RxAuditActionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RxAuditActionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
