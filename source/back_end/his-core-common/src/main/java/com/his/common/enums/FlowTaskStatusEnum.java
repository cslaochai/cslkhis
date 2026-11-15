package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 流程待办状态（biz_flow_task.task_status）。
 */
@Getter
public enum FlowTaskStatusEnum {

    PENDING("PENDING", "待处理"),
    PASS("PASS", "已通过"),
    REJECT("REJECT", "已驳回"),
    CANCELED("CANCELED", "已作废");

    private final String code;
    private final String label;

    FlowTaskStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FlowTaskStatusEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (FlowTaskStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        FlowTaskStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        FlowTaskStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        FlowTaskStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
