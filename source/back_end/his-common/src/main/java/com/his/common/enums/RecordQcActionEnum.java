package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 病历质控流转动作枚举
 */
@Getter
public enum RecordQcActionEnum {

    START(1, "发起送审"),
    APPROVE(2, "审核通过"),
    RETURN(3, "退回整改"),
    RESUBMIT(4, "整改提交"),
    FINAL(5, "终审通过");

    private final int code;
    private final String label;

    RecordQcActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcActionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RecordQcActionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RecordQcActionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RecordQcActionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RecordQcActionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
