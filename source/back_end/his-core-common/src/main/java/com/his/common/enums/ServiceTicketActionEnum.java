package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 服务工单流转动作（0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开）。
 *
 * <p>写进流转记录供患者端时间轴与院内证据链展示，只增不改。
 */
@Getter
public enum ServiceTicketActionEnum {

    SUBMIT(0, "提交工单"),
    ACCEPT(1, "客服受理"),
    REPLY(2, "客服回复"),
    FINISH(3, "办理完成"),
    APPEND(4, "补充留言"),
    CLOSE(5, "关闭工单"),
    CANCEL(6, "患者撤单"),
    REOPEN(7, "患者重开");

    private final int code;
    private final String label;

    ServiceTicketActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static ServiceTicketActionEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ServiceTicketActionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ServiceTicketActionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ServiceTicketActionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ServiceTicketActionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
