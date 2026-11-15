package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 流程实例状态（biz_flow_instance.flow_status）。
 */
@Getter
public enum FlowInstanceStatusEnum {

    RUNNING("RUNNING", "运行中"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回"),
    CANCELED("CANCELED", "已撤回");

    private final String code;
    private final String label;

    FlowInstanceStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FlowInstanceStatusEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (FlowInstanceStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        FlowInstanceStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        FlowInstanceStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        FlowInstanceStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
