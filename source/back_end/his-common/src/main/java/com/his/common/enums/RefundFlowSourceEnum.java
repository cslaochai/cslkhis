package com.his.common.enums;

/**
 * 退费流水来源枚举（字典 {@code his_refund_flow_source}，落在退费记录单的流水来源列）。
 *
 * <p>台账必须自带来源：三条写入路径的**审批链完全不同**（申请单要走审核+执行、收费处直退没人审、
 * 退号是挂号模块联动触发的），没有来源列就没法解释"这笔钱是谁批出去的"，
 * 也正是"退号联动的退费在退费管理页永远查不到"这个问题。
 */
public enum RefundFlowSourceEnum {

    SEED(0, "存量铺底"),
    REFUND_APPLY(1, "退费申请执行"),
    DIRECT(2, "收费处直退"),
    CANCEL_REGIST(3, "退号联动退费");

    private final Integer code;
    private final String desc;

    RefundFlowSourceEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundFlowSourceEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundFlowSourceEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
