package com.his.common.enums;

import lombok.Getter;

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

    public static ServiceTicketActionEnum fromCode(int code) {
        for (ServiceTicketActionEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内（脏数据）一律返回空串
     */
    public static String getText(Integer code) {
        ServiceTicketActionEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ServiceTicketActionEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
