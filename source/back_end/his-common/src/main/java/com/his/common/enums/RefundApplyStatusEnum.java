package com.his.common.enums;

/**
 * 退费申请状态枚举（字典 {@code his_refund_apply_status}，落在退费申请单的申请状态列）。
 *
 * <p>5-已作废是本轮补的：审核通过(2) 的申请在执行前发现退不动（金额落不到明细边界、
 * 明细已被别的退费吃掉），原先既退不回去也删不掉，而判重又拦着这张收费单
 * —— 一笔钱就此永久卡在"待执行"上，只能来库里改状态。
 */
public enum RefundApplyStatusEnum {

    PENDING_AUDIT(1, "待审核"),
    AUDIT_PASSED(2, "审核通过"),
    AUDIT_REJECTED(3, "审核驳回"),
    REFUNDED(4, "已退费"),
    DISCARDED(5, "已作废");

    private final Integer code;
    private final String desc;

    RefundApplyStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundApplyStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundApplyStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 还"活着"的申请（未被执行、未被驳回、未被作废）：同一张收费单只允许有一条，且作废只允许作用在这两态
     */
    public static boolean isInflight(Integer code) {
        return PENDING_AUDIT.getCode().equals(code) || AUDIT_PASSED.getCode().equals(code);
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
