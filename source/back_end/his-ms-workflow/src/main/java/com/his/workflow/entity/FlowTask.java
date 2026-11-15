package com.his.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批待办（biz_flow_task）：每个审批节点按候选人各建一行。
 */
@Data
@TableName("biz_flow_task")
public class FlowTask {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long definitionId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long instanceId;

    private String flowCode;

    private String flowName;

    private String businessId;

    private String nodeCode;

    private String nodeName;

    /** 节点类型，见 FlowNodeTypeEnum */
    private Integer nodeType;

    /** 待办状态（PENDING/PASS/REJECT/CANCELED），见 FlowTaskStatusEnum */
    private String taskStatus;

    /** 候选人表达式快照（role:xxx / user:xxx，多个 @@ 分隔），待办列表按它匹配 */
    private String assigneeExpr;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assigneeUserId;

    private String assigneeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
