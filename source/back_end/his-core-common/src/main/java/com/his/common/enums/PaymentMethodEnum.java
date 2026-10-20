package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

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

    public static PaymentMethodEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PaymentMethodEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PaymentMethodEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PaymentMethodEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        PaymentMethodEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
