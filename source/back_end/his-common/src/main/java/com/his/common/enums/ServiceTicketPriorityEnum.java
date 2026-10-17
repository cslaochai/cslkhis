package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static ServiceTicketPriorityEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ServiceTicketPriorityEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ServiceTicketPriorityEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ServiceTicketPriorityEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ServiceTicketPriorityEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
