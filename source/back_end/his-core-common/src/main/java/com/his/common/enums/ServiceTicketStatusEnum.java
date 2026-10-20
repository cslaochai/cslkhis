package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 服务工单状态（0-待受理 1-处理中 2-已办结 3-已关闭）。
 *
 * <p>状态机的唯一事实来源：终态判定与「患者还能做哪些动作」都收在这里，
 * 新增档位只需补一个常量，不用改各处 switch。
 */
@Getter
public enum ServiceTicketStatusEnum {

    /**
     * 待受理：患者已提交，无人认领
     */
    WAIT_ACCEPT(0, "待受理"),

    /**
     * 处理中：客服已受理
     */
    HANDLING(1, "处理中"),

    /**
     * 已办结：客服给了处理结果，等患者确认
     */
    FINISHED(2, "已办结"),

    /**
     * 已关闭：患者确认解决 / 患者撤单 / 客服关闭（终态）
     */
    CLOSED(3, "已关闭");

    private final int code;
    private final String label;

    ServiceTicketStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 已关闭是终态，不能再动（患者想再问就重新提单）
     */
    public static boolean isClosed(Integer code) {
        return code != null && code == CLOSED.code;
    }

    /**
     * 患者还能补充留言：办结后补充等于「问题没解决」，调用方要据此自动重开
     */
    public static boolean canAppend(Integer code) {
        return code != null && (code == WAIT_ACCEPT.code || code == HANDLING.code || code == FINISHED.code);
    }

    /**
     * 患者可撤单：还没办结的都能撤
     */
    public static boolean canCancel(Integer code) {
        return code != null && (code == WAIT_ACCEPT.code || code == HANDLING.code);
    }

    /**
     * 患者可确认解决 / 重开：只在办结态
     */
    public static boolean canConfirm(Integer code) {
        return code != null && code == FINISHED.code;
    }

    public static ServiceTicketStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ServiceTicketStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ServiceTicketStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ServiceTicketStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ServiceTicketStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
