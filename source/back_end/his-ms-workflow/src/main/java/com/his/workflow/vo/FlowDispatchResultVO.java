package com.his.workflow.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一入口分发结果。
 */
@Data
public class FlowDispatchResultVO implements Serializable {

    /** 实际启动的流程编码（意图识别结果） */
    private String flowCode;

    private String flowName;

    /** 意图识别理由，便于复盘为什么走了这条流程 */
    private String routeReason;

    /** 识别置信度 */
    private Double routeConfidence;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long instanceId;

    private String nodeCode;

    private String nodeName;

    private Integer nodeType;

    private String flowStatus;

    private String statusLabel;

    /**
     * 最后一个自动节点的问答输出（患者端直接展示用），没有则为空
     */
    private String answer;

    private String variableJson;
}
