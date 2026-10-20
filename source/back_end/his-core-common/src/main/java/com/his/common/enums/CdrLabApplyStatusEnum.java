package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrLabApplyStatusEnum {
    SUBMITTED(1, "已提交"),
    PAID(2, "已缴费"),
    SAMPLED(3, "已采样"),
    TESTING(4, "检验中"),
    REPORTED(5, "已出报告"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String label;

    CdrLabApplyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrLabApplyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrLabApplyStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrLabApplyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrLabApplyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrLabApplyStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
