package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 自动/网关节点的 AI 动作（sys_flow_node.ai_action）。
 * <p>CAPABILITY = 调一次提示词模板拿结构化结果；ROUTE = 意图路由，从候选流程里选一条。
 */
@Getter
public enum FlowAiActionEnum {

    CAPABILITY("CAPABILITY", "能力调用"),
    ROUTE("ROUTE", "意图路由");

    private final String code;
    private final String label;

    FlowAiActionEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FlowAiActionEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (FlowAiActionEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        FlowAiActionEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        FlowAiActionEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        FlowAiActionEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
