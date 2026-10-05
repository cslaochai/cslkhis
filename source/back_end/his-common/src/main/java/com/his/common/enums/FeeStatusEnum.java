package com.his.common.enums;

/**
 * 费用记账状态（字典 {@code his_fee_status}，落在费用记账流水的记账状态列）。
 *
 * <p>本层是应收的唯一来源，金额列一经写入不再 UPDATE：错账不就地改数，
 * 只写一条负数红冲行并把原行置为 {@link #REVERSED}，所以没有"已修改"这个态。
 */
public enum FeeStatusEnum {

    /**
     * 可被结算账单锁定
     */
    PENDING(1, "待结算"),
    /**
     * 已被某张账单捞走（bill_id 非空），不能进第二张账单
     */
    LOCKED(2, "已锁定"),
    /**
     * 随账单结清，费用生命周期结束
     */
    SETTLED(3, "已结算"),
    /**
     * 被红冲行冲销，本行不再计入应收
     */
    REVERSED(4, "已红冲");

    private final Integer code;
    private final String desc;

    FeeStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FeeStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FeeStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String descOf(Integer code) {
        FeeStatusEnum item = fromCode(code);
        return item == null ? "未知状态" : item.desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
