package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

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

    public static FeeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FeeStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        FeeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FeeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }
}
