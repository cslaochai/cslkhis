package com.his.common.enums;

/**
 * 支付流水来源（字典 his_txn_source，落在支付资金流水的来源类型列）。
 */
public enum TxnSourceEnum {

    CASHIER(1, "收费台收款"),
    MINIAPP(2, "患者端支付"),
    PREPAY(3, "住院预交金"),
    REFUND_APPLY(4, "退费申请执行"),
    DIRECT_REFUND(5, "收费处直退"),
    CANCEL_REGIST(6, "退号联动退费"),
    DISCHARGE_DIFF(7, "出院结算退差"),
    ACCOUNT_BALANCE(8, "账户余额抵扣"),
    MANUAL(9, "手工补账");

    private final Integer code;
    private final String desc;

    TxnSourceEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TxnSourceEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TxnSourceEnum item : values()) {
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
        TxnSourceEnum item = fromCode(code);
        return item == null ? "未知来源" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 是否退款类来源（决定要不要带 orig_txn_id 与 refund_method）
     */
    public boolean refundKind() {
        return this == REFUND_APPLY || this == DIRECT_REFUND || this == CANCEL_REGIST || this == DISCHARGE_DIFF;
    }
}
