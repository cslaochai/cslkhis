package com.his.common.enums;

/**
 * 资金方向（字典 his_pay_direction，落在支付资金流水的方向列、
 */
public enum PayDirectionEnum {

    CHARGE(1, "收款"),
    REFUND(2, "退款");

    private final Integer code;
    private final String desc;

    PayDirectionEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayDirectionEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayDirectionEnum item : values()) {
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
        PayDirectionEnum item = fromCode(code);
        return item == null ? "未知方向" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 该方向在金额上该带的符号（收款为正、退款为负）
     */
    public int sign() {
        return this == CHARGE ? 1 : -1;
    }
}
