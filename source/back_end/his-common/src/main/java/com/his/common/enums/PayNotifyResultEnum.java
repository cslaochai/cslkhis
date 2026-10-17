package com.his.common.enums;

import lombok.Getter;

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

    public static PayNotifyResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayNotifyResultEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        PayNotifyResultEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        PayNotifyResultEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
