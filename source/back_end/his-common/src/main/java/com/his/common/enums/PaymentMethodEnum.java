package com.his.common.enums;

/**
 * 支付方式枚举（字典 his_pay_method，落在支付资金流水的支付方式列）。
 */
public enum PaymentMethodEnum {

    CASH(1, "现金"),
    WECHAT(2, "微信"),
    ALIPAY(3, "支付宝"),
    INSURANCE_ACCOUNT(4, "医保个账"),
    BALANCE(5, "院内余额"),
    BANK(6, "银行卡"),
    TRANSFER(7, "转账");

    private final Integer code;
    private final String desc;

    PaymentMethodEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 取枚举；未知/为空返回 null（调用方按"渠道不明"兜底成现金退回，不抛错）
     */
    public static PaymentMethodEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentMethodEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        PaymentMethodEnum item = getByCode(code);
        return item == null ? "" : item.desc;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        PaymentMethodEnum item = code == null ? null : getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 是否走第三方渠道（原路退回必须找回渠道那笔钱，现金/余额/医保个账各走各的出口）
     */
    public boolean channelBacked() {
        return this == WECHAT || this == ALIPAY || this == BANK;
    }
}
