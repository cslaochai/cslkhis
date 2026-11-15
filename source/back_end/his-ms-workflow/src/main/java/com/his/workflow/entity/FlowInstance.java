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
 * 流程实例（biz_flow_instance）：一张业务单据跑一次流程的记录，保存当前节点与变量。
 */
@Data
@TableName("biz_flow_instance")
public class FlowInstance {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long definitionId;

    private String flowCode;

    private String flowName;

    /** 业务单据 ID（字符串，兼容各模块主键形态） */
    private String businessId;

    /** 业务类型（如 INSURANCE_SETTLEMENT/EMR_QC/PATIENT_CONSULT） */
    private String bizType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long startedByUserId;

    private String startedByName;

    private String nodeCode;

    private String nodeName;

    /** 当前节点类型，见 FlowNodeTypeEnum */
    private Integer nodeType;

    /** 实例状态（RUNNING/APPROVED/REJECTED/CANCELED），见 FlowInstanceStatusEnum */
    private String flowStatus;

    /** 活动标记（1-活动 0-已结束），便于按实例查待办 */
    private Integer activityStatus;

    /** 流程变量 JSON：入口入参 + 各自动节点输出，键为节点编码 */
    private String variableJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
