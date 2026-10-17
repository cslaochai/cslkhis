package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 渠道异步通知的处理结果
 */
@Getter
public enum PayNotifyResultEnum {

    BOOKED(1, "已入账"),
    DUPLICATED(2, "重复通知"),
    NOT_PAID(3, "未付款通知"),
    SIGN_INVALID(4, "验签失败"),
    ORDER_MISSING(5, "支付单不存在"),
    CHANNEL_MISMATCH(6, "渠道不符"),
    AMOUNT_INVALID(7, "金额非法"),
    REJECTED(8, "通知不受理"),
    BOOK_FAILED(9, "入账异常");

    private final Integer code;
    private final String label;

    PayNotifyResultEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static PayNotifyResultEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PayNotifyResultEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PayNotifyResultEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PayNotifyResultEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PayNotifyResultEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
