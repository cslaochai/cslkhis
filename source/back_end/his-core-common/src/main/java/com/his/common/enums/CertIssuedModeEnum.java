package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 证书签发方式。
 */
public enum CertIssuedModeEnum {

    MANUAL(1, "人工签发"),
    AUTO(2, "系统自动签发");

    private final int code;
    private final String label;

    CertIssuedModeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static CertIssuedModeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CertIssuedModeEnum item : values()) {
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
        CertIssuedModeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CertIssuedModeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
