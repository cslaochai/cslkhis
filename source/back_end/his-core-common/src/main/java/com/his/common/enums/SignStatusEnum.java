package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 签名记录自身的状态。
 */
public enum SignStatusEnum {

    VALID(1, "有效"),
    INVALID(2, "已作废");

    private final int code;
    private final String label;

    SignStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static SignStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SignStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        SignStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SignStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
