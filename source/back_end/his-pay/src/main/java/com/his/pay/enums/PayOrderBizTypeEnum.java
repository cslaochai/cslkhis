package com.his.pay.enums;

import lombok.Getter;

/**
 * 支付订单业务类型
 */
@Getter
public enum PayOrderBizTypeEnum {

    OUTPATIENT_PAY(1, "门诊缴费"),
    REGISTER_FEE(2, "挂号费"),
    INPATIENT_DEPOSIT(3, "住院押金");

    private final Integer code;
    private final String label;

    PayOrderBizTypeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PayOrderBizTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayOrderBizTypeEnum item : values()) {
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
        PayOrderBizTypeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        PayOrderBizTypeEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
