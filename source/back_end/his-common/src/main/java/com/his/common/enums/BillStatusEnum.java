package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 结算账单状态（字典 his_bill_status，落在结算账单的账单状态列）。
 */
public enum BillStatusEnum {

    UNPAID(1, "待支付"),
    PARTIAL_PAID(2, "部分支付"),
    PAID(3, "已支付"),
    VOIDED(4, "已作废"),
    REFUNDED(5, "已退费");

    private final Integer code;
    private final String desc;

    BillStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 已收回的钱还能不能再收（作废/退完的单一率拒收，防止重复收同一笔钱）
     */
    public boolean payable() {
        return this == UNPAID || this == PARTIAL_PAID;
    }

    public static BillStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BillStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        BillStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BillStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }
}
