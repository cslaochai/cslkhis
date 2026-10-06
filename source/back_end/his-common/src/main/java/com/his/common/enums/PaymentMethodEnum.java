package com.his.common.enums;

/**
 * 支付方式枚举（字典 {@code his_pay_method}，落在支付资金流水的支付方式列）。
 *
 * <p>建表至今没有对应枚举，各处按裸数字判断（{@code sql/119} 的注释里甚至把 4 写成"银行卡"，
 * 与字典的"医保(卡)"漂移）。退费要按支付方式决定"钱从哪条路回去"，
 * 那份映射不能再抄裸数字，所以在这里立一个权威。
 *
 * <p>收费四层重构（sql/125）后本枚举只描述<b>真实进出本院的钱</b>：
 * {@link #INSURANCE_ACCOUNT} 是刷医保卡扣的<b>个人账户</b>额度，
 * 医保统筹是后付给医保局的钱，绝不是一种支付方式（它记在账单的统筹金额上，
 * 不进收银员的现金清点，否则班结必然多出一块说不来的差额）。
 * 6/7 两个码值把原先只在预交金侧自成一派的"银行卡/转账"并进来，全院一套口径。
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

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
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

    /**
     * 是否走第三方渠道（原路退回必须找回渠道那笔钱，现金/余额/医保个账各走各的出口）
     */
    public boolean channelBacked() {
        return this == WECHAT || this == ALIPAY || this == BANK;
    }
}
