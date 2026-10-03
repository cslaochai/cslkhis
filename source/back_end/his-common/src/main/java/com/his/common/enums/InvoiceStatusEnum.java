package com.his.common.enums;

/**
 * 发票状态（字典 {@code his_invoice_status}，落在发票的发票状态列）。
 *
 * <p>票据层是 L4，只对账单不碰钱：账单退了钱，票不能就地改金额，只能作废或红冲换开
 * （{@code orig_invoice_id} 指着被冲的那张）。"点一下就改状态"在财政序列号上是伪造票据。
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

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 还算是一张有效票（能被日结计成"已开票"） */
    public boolean live() {
        return this == ISSUED || this == PRINTED;
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

    public static String descOf(Integer code) {
        InvoiceStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
    }
}
