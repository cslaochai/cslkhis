package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 资金账户流水类型（字典 his_account_txn_type，落在资金账户流水的流水类型列）。
 */
public enum AccountTxnTypeEnum {

    PREPAY_RECHARGE(1, "住院预交金充值"),
    PREPAY_REFUND(2, "预交金退款"),
    BALANCE_PAY(3, "余额支付扣减"),
    BALANCE_REFUND(4, "余额退款入账"),
    DISCHARGE_DIFF_IN(5, "出院结算退差入账"),
    MANUAL_ADJUST(6, "手工调整");

    private final Integer code;
    private final String desc;

    AccountTxnTypeEnum(Integer code, String desc) {
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
     * 扣用类（钱从账户走出去：余额支付、预交金退给患者），流水记负；
     * 其余为入账类记正。手工调整是唯一允许调用方自带符号的类型。
     */
    public boolean debit() {
        return this == BALANCE_PAY || this == PREPAY_REFUND;
    }

    public static AccountTxnTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AccountTxnTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        AccountTxnTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AccountTxnTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }
}
