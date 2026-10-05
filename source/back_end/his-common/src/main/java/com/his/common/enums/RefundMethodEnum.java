package com.his.common.enums;

/**
 * 退费方式枚举（字典 {@code his_refund_method}，落在退费记录单的退费方式列）。
 *
 * <p>"原路退回"不是随手写的描述，而是由**原支付方式**唯一决定的，映射只写在这一处
 * （{@link #ofPayMethod}）：各处再抄一份 switch 迟早漂移，漂移的结果是把微信收的钱退成现金，
 * 渠道侧长款、日结平不上，且没有任何地方会报错。
 */
public enum RefundMethodEnum {

    ORIGINAL(1, "原路退回"),
    CASH(2, "现金退回"),
    BALANCE(3, "余额退回");

    private final Integer code;
    private final String desc;

    RefundMethodEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 原支付方式 → 退费方式。
     *
     * <p>医保个账(4) 也记"原路退回"：钱要退回参保人的卡账户，报盘侧由 2305 撤销报文负责，
     * 台账上不必另立码（另立就等于要求医保局认我们的字典）。
     * 现金走柜面点钞、余额退回患者院内账户，都不算"原路退回"。
     */
    public static RefundMethodEnum ofPayMethod(Integer payMethodCode) {
        PaymentMethodEnum payMethod = PaymentMethodEnum.getByCode(payMethodCode);
        if (payMethod == null) {
            return CASH;
        }
        return switch (payMethod) {
            case WECHAT, ALIPAY, BANK, TRANSFER, INSURANCE_ACCOUNT -> ORIGINAL;
            case BALANCE -> BALANCE;
            case CASH -> CASH;
        };
    }

    /**
     * 这笔退费是否要走支付渠道（商户平台）退回。微信/支付宝/银行卡刷卡是线上收的，必须线上退；
     * 转账(7) 是线下汇款，只能人工退回对公账户，所以"原路退回"却不走渠道口子；
     * 医保走报盘撤销、现金与院内余额走柜面/账户，同理都不该把请求发给渠道。
     */
    public static boolean viaChannel(Integer payMethodCode) {
        PaymentMethodEnum payMethod = PaymentMethodEnum.getByCode(payMethodCode);
        return payMethod != null && payMethod.channelBacked();
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
