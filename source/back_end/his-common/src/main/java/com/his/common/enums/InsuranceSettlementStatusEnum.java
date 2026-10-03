package com.his.common.enums;

/**
 * 医保结算清单状态（字典 {@code his_ins_settlement_status}，落在医保结算清单的结算状态列）。
 *
 * <p>清单是 L2 出账的产物：账单结算时生成（待结算），钱收齐后由院内结算算出统筹/个账/自付三个真数，
 * 再报盘给医保（已上传），医保侧审核（已审核）。
 * {@link #VOIDED} 是本次补的：账单作废或整单退费后清单必须跟着作废，否则它会永远留在
 * 「待结算」列表里，等着被人点一次结算、报一张已经不存在账单的 2304。
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

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 报盘在医保侧还挂着账：这类清单要退钱，必须先用 2305 把它撤回来，不能直接作废 */
    public boolean uploaded() {
        return this == UPLOADED || this == AUDITED;
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

    public static String descOf(Integer code) {
        InsuranceSettlementStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
    }
}
