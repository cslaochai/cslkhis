package com.his.miniapp.enums;

import lombok.Getter;

/**
 * 患者端统一支付单业务类型枚举（1-门诊缴费 2-挂号费 3-住院押金，口径 = biz_pay_order.biz_type）。
 */
@Getter
public enum PayBizTypeEnum {

    OPD_PAYMENT(1, "门诊缴费"),
    REGIST_FEE(2, "挂号费"),
    DEPOSIT(3, "住院押金");

    private final int code;
    private final String label;

    PayBizTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PayBizTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayBizTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        PayBizTypeEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        PayBizTypeEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
