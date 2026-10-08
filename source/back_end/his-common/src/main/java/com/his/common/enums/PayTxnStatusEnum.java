package com.his.common.enums;

/**
 * 支付流水状态（字典 his_pay_txn_status，落在支付资金流水的流水状态列）。
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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String descOf(Integer code) {
        PayTxnStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
