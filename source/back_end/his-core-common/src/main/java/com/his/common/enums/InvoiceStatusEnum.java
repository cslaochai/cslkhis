package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 发票状态（字典 his_invoice_status，落在发票的发票状态列）。
 */
public enum InvoiceStatusEnum {

    ISSUED(1, "已开具"),
    PRINTED(2, "已打印"),
    VOIDED(3, "已作废"),
    REVERSED(4, "已红冲换开");

    private final Integer code;
    private final String desc;

    InvoiceStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 还算是一张有效票（能被日结计成"已开票"）
     */
    public boolean live() {
        return this == ISSUED || this == PRINTED;
    }

    public static InvoiceStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InvoiceStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        InvoiceStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InvoiceStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }
}
