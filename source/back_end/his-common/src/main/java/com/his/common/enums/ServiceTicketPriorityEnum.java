package com.his.common.enums;

import lombok.Getter;

/**
 * 服务工单优先级（0-普通 1-紧急）。
 *
 * <p>与 {@link BedPriorityEnum}（床位等待 1-普通 2-急 3-危重）、复诊收费策略里的数字排序权重
 * 是三个不同口径，码值互不等价，别混用。
 */
@Getter
public enum ServiceTicketPriorityEnum {

    NORMAL(0, "普通"),
    URGENT(1, "紧急");

    private final int code;
    private final String label;

    ServiceTicketPriorityEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ServiceTicketPriorityEnum fromCode(int code) {
        for (ServiceTicketPriorityEnum item : values()) {
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
        ServiceTicketPriorityEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ServiceTicketPriorityEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
