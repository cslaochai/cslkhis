package com.his.common.enums;

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

    public static InvoiceStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InvoiceStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String descOf(Integer code) {
        InvoiceStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
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
}
