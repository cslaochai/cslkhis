package com.his.common.enums;

/**
 * 资金账户流水类型（字典 {@code his_account_txn_type}，落在资金账户流水的流水类型列）。
 *
 * <p>账户只回答"这里有多少钱"，账单只回答"该收多少"，两者靠支付流水相连：
 * 余额抵扣在账户这边是一条扣减流水、在支付那边是一笔 pay_method=5 的收款流水，
 * 两条记录同一时刻同事务落库，缺一对不上就是事故。
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

    public static AccountTxnTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AccountTxnTypeEnum item : values()) {
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
        AccountTxnTypeEnum item = fromCode(code);
        return item == null ? "未知类型" : item.desc;
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
}
