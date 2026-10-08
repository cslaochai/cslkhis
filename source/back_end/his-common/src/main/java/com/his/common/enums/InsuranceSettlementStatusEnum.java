package com.his.common.enums;

/**
 * 医保结算清单状态（字典 his_ins_settlement_status，落在医保结算清单的结算状态列）。
 */
public enum InsuranceSettlementStatusEnum {

    PENDING(1, "待结算"),
    SETTLED(2, "已结算"),
    UPLOADED(3, "已上传"),
    AUDITED(4, "已审核"),
    VOIDED(5, "已作废");

    private final Integer code;
    private final String desc;

    InsuranceSettlementStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InsuranceSettlementStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InsuranceSettlementStatusEnum item : values()) {
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
        InsuranceSettlementStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 报盘在医保侧还挂着账：这类清单要退钱，必须先用 2305 把它撤回来，不能直接作废
     */
    public boolean uploaded() {
        return this == UPLOADED || this == AUDITED;
    }
}
