package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 支付渠道（患者端支付单与收费台扫码共用的渠道编号）。
 */
@Getter
public enum PayChannelEnum {

    WECHAT(1, "微信"),
    ALIPAY(2, "支付宝"),
    UNIONPAY(3, "云闪付");

    private final Integer code;
    private final String label;

    PayChannelEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 渠道 → 收款方式（同一笔钱在渠道侧和本库资金流水里的两个名字，落 L3 流水时必须换一次口径）。
     *
     * <p>云闪付走银行卡收单，故落 6-银行卡；取不到对应收款方式时返回 null，由调用方拒单，
     * 不要兜底成现金 —— 那会让现金抽屉当天凭空多出一笔。
     */
    public static PaymentMethodEnum payMethodOf(Integer code) {
        PayChannelEnum item = fromCode(code);
        if (item == null) {
            return null;
        }
        return switch (item) {
            case WECHAT -> PaymentMethodEnum.WECHAT;
            case ALIPAY -> PaymentMethodEnum.ALIPAY;
            case UNIONPAY -> PaymentMethodEnum.BANK;
        };
    }

    public static PayChannelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PayChannelEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PayChannelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PayChannelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PayChannelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
