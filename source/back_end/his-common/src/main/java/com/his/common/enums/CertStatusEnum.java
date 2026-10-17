package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 员工签名证书状态。
 */
public enum CertStatusEnum {

    ACTIVE(1, "有效"),
    REVOKED(2, "已吊销");

    private final int code;
    private final String label;

    CertStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static CertStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CertStatusEnum item : values()) {
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
        CertStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CertStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
