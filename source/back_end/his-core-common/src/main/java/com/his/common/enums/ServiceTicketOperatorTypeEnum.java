package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static ServiceTicketOperatorTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ServiceTicketOperatorTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ServiceTicketOperatorTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ServiceTicketOperatorTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ServiceTicketOperatorTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
