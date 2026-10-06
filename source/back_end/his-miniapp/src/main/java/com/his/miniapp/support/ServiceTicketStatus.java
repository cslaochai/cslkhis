package com.his.miniapp.support;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 工单状态与流转动作的<b>唯一口径</b>（建表见 {@code sql/221}）。
 *
 * <p><b>为什么单独拎一个类</b>：这张表原来就有 status（0待处理/1已处理/2已关闭），
 * 工单化后语义变了（1 从"已处理"变成"处理中"）。口径散在 Service 和前端各写一份，
 * 必然出现"后端认为在处理中、前端显示已处理"。这里改一次，两边都跟着改。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceTicketStatus {

    /** 待受理：患者已提交，无人认领 */
    public static final int WAIT_ACCEPT = 0;

    /** 处理中：客服已受理 */
    public static final int HANDLING = 1;

    /** 已办结：客服给了处理结果，等患者确认 */
    public static final int FINISHED = 2;

    /** 已关闭：患者确认解决 / 患者撤单 / 客服关闭（终态） */
    public static final int CLOSED = 3;

    // 流转动作
    public static final int ACT_SUBMIT = 0;
    public static final int ACT_ACCEPT = 1;
    public static final int ACT_REPLY = 2;
    public static final int ACT_FINISH = 3;
    public static final int ACT_APPEND = 4;
    public static final int ACT_CLOSE = 5;
    public static final int ACT_CANCEL = 6;
    public static final int ACT_REOPEN = 7;

    public static String statusText(Integer status) {
        if (status == null) {
            return "待受理";
        }
        return switch (status) {
            case HANDLING -> "处理中";
            case FINISHED -> "已办结";
            case CLOSED -> "已关闭";
            default -> "待受理";
        };
    }

    public static String actionText(Integer action) {
        if (action == null) {
            return "记录";
        }
        return switch (action) {
            case ACT_SUBMIT -> "提交工单";
            case ACT_ACCEPT -> "客服受理";
            case ACT_REPLY -> "客服回复";
            case ACT_FINISH -> "办理完成";
            case ACT_APPEND -> "补充留言";
            case ACT_CLOSE -> "关闭工单";
            case ACT_CANCEL -> "患者撤单";
            case ACT_REOPEN -> "患者重开";
            default -> "记录";
        };
    }

    /** 已关闭是终态，不能再动（患者想再问就重新提单） */
    public static boolean isClosed(Integer status) {
        return status != null && status == CLOSED;
    }

    /** 患者还能补充留言：办结后补充等于"问题没解决"，会自动重开 */
    public static boolean canAppend(Integer status) {
        return status != null && (status == WAIT_ACCEPT || status == HANDLING || status == FINISHED);
    }

    /** 患者可撤单：还没办结的都能撤 */
    public static boolean canCancel(Integer status) {
        return status != null && (status == WAIT_ACCEPT || status == HANDLING);
    }

    /** 患者可确认解决：只在办结态 */
    public static boolean canConfirm(Integer status) {
        return status != null && status == FINISHED;
    }
}
