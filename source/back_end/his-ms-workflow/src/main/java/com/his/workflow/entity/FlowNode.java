package com.his.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 流程节点（sys_flow_node）。
 */
@Data
@TableName("sys_flow_node")
public class FlowNode {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long definitionId;

    /** 节点编码，同一流程内唯一 */
    private String nodeCode;

    private String nodeName;

    /** 节点类型（0-开始 1-审批 2-结束 3-自动 4-网关），见 FlowNodeTypeEnum */
    private Integer nodeType;

    /** 审批人表达式，多个用 @@ 分隔，如 role:DOCTOR@@user:1001 */
    private String permissionFlag;

    /** 签核比例：0 或签、100 会签、n 票签 n%（仅审批节点） */
    private String nodeRatio;

    /** AI 动作（CAPABILITY-能力调用 ROUTE-意图路由），见 FlowAiActionEnum；自动/网关节点可空 */
    private String aiAction;

    /** 能力标识，空则用默认 flow_ai_node */
    private String aiCapabilityKey;

    /** 提示词模板编码 */
    private String aiTemplateName;

    /** 节点固定输入 JSON（与实例变量合并后作为提示词输入） */
    private String aiInputJson;

    /** 画布坐标，仅前端使用 */
    private String coordinate;

    /** 展示顺序 */
    private Integer sortNo;
}
