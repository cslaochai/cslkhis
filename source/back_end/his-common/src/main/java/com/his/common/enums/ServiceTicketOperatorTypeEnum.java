package com.his.common.enums;

import lombok.Getter;

/**
 * 服务工单流转记录的操作人类型（1-患者 2-院内）。
 */
@Getter
public enum ServiceTicketOperatorTypeEnum {

    PATIENT(1, "患者"),
    HOSPITAL(2, "院内");

    private final int code;
    private final String label;

    ServiceTicketOperatorTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ServiceTicketOperatorTypeEnum fromCode(int code) {
        for (ServiceTicketOperatorTypeEnum item : values()) {
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
        ServiceTicketOperatorTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ServiceTicketOperatorTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
