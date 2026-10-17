package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 最近一次验签的结果。
 */
public enum SignVerifyStatusEnum {

    UNCHECKED(0, "未校验"),
    PASSED(1, "验签通过"),
    FAILED(2, "验签失败");

    private final int code;
    private final String label;

    SignVerifyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static SignVerifyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (SignVerifyStatusEnum item : values()) {
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
        SignVerifyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        SignVerifyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
