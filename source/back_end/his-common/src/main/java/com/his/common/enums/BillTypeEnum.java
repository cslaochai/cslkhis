package com.his.common.enums;

/**
 * 结算账单类型（字典 {@code his_bill_type}，落在结算账单的账单类型列）。
 *
 * <p>区分"中途结算"与"出院结算"是住院侧的关键：中途结算把已发生的记账行锁定收钱，
 * 出院结算要连预交金账户一起结清（退差/补欠），两者动作与票据都不同。
 */
public enum BillTypeEnum {

    REGISTRATION(1, "挂号费结算"),
    OUTPATIENT(2, "门诊诊间结算"),
    INPATIENT_MID(3, "住院中途结算"),
    DISCHARGE(4, "出院结算");

    private final Integer code;
    private final String desc;

    BillTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static BillTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BillTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String descOf(Integer code) {
        BillTypeEnum item = fromCode(code);
        return item == null ? "未知类型" : item.desc;
    }
}
