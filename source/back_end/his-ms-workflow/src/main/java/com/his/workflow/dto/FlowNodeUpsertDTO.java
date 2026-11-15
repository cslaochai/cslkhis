package com.his.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程节点新增/修改。
 */
@Data
public class FlowNodeUpsertDTO implements Serializable {

    @NotBlank(message = "节点编码不能为空")
    private String nodeCode;

    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    /** 节点类型（0-开始 1-审批 2-结束 3-自动 4-网关） */
    @NotNull(message = "节点类型不能为空")
    private Integer nodeType;

    /** 审批人表达式，多个用 @@ 分隔，如 role:DOCTOR@@user:1001 */
    private String permissionFlag;

    /** 签核比例：0 或签、100 会签、n 票签 n% */
    private String nodeRatio;

    /** AI 动作（CAPABILITY/ROUTE），自动与网关节点用 */
    private String aiAction;

    /** 能力标识，空则用默认 flow_ai_node */
    private String aiCapabilityKey;

    /** 提示词模板编码 */
    private String aiTemplateName;

    /** 节点固定输入 JSON */
    private String aiInputJson;

    private String coordinate;

    private Integer sortNo;
}
