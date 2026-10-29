package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 员工签名证书状态。
 */
public enum CertStatusEnum {

    /**
     * 已生成密钥对、等待线下把公钥送 CA 签发并回填证书（尚不可用于签名）
     */
    PENDING_CA(0, "待CA签发"),
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
