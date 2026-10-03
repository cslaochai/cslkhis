package com.his.charge.constant;

/**
 * 医保扣款状态码（与 sql/163 的字典 his_yb_deduct_status 逐字对齐）。
 *
 * <p>字典给前端翻译名字，这里给服务端做状态机判定 —— 两边都以 163 为唯一事实来源。
 */
public final class YbDeductStatus {

    /**
     * 待确认：收到通知尚未申诉或确认，唯一可编辑/可作废的状态
     */
    public static final int PENDING_CONFIRM = 1;

    /**
     * 申诉中：已提交材料，等医保局回复
     */
    public static final int APPEALING = 2;

    /**
     * 申诉成功：医保局撤销扣款，无需缴回
     */
    public static final int APPEAL_SUCCESS = 3;

    /**
     * 维持扣款待缴：申诉驳回或直接确认，待向医保基金缴回
     */
    public static final int WAIT_PAY = 4;

    /**
     * 已缴回：资金退回医保基金，闭环
     */
    public static final int PAID_BACK = 5;

    /**
     * 已作废：误录/重复录入
     */
    public static final int CANCELLED = 6;

    private YbDeductStatus() {
    }
}
