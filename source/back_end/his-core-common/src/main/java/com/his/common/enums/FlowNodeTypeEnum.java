package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 流程节点类型（sys_flow_node.node_type）。
 * <p>0/1/2 沿用 CA 工作流引擎语义；3、4 为 cslk 扩展：自动节点与网关节点。
 */
@Getter
public enum FlowNodeTypeEnum {

    START(0, "开始"),
    APPROVAL(1, "审批"),
    END(2, "结束"),
    /** 自动节点：无人工待办，调用 AI 能力/提示词后自动推进 */
    AUTO(3, "自动"),
    /** 网关节点：按流转条件分支，也可先做一次意图路由再分支 */
    GATEWAY(4, "网关");

    private final int code;
    private final String label;

    FlowNodeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FlowNodeTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FlowNodeTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        FlowNodeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FlowNodeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        FlowNodeTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
