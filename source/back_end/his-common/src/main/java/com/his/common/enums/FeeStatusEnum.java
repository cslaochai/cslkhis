package com.his.common.enums;

/**
 * 费用记账状态（字典 his_fee_status，落在费用记账流水的记账状态列）。
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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
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
