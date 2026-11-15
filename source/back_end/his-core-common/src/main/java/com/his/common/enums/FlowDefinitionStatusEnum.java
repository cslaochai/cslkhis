package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 流程定义状态（sys_flow_definition.status）：仅已发布可被启动。
 */
@Getter
public enum FlowDefinitionStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    DISABLED(2, "已停用");

    private final int code;
    private final String label;

    FlowDefinitionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FlowDefinitionStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FlowDefinitionStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        FlowDefinitionStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FlowDefinitionStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        FlowDefinitionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
