package com.his.common.enums;

/**
 * 支付流水状态（字典 {@code his_pay_txn_status}，落在支付资金流水的流水状态列）。
 *
 * <p>流水错了不删也不改金额：置 REVERSED 并另起一笔反向流水，两边用 {@code orig_txn_id} 互指。
 * 删流水等于把"收过钱"这个事实抹掉，钱货两讫的追溯链就断了。
 */
public enum PayTxnStatusEnum {

    SUCCESS(1, "成功"),
    REVERSED(2, "已冲正");

    private final Integer code;
    private final String desc;

    PayTxnStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PayTxnStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayTxnStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String descOf(Integer code) {
        PayTxnStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
    }
}
