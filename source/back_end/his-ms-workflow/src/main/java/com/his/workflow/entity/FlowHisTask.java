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
 * 流转轨迹（biz_flow_his_task）：只增不改，谁在什么节点做了什么。
 */
@Data
@TableName("biz_flow_his_task")
public class FlowHisTask {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long definitionId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long instanceId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private String businessId;

    private String flowCode;

    private String nodeCode;

    private String nodeName;

    private Integer nodeType;

    /** 动作（START-发起 PASS-通过 REJECT-驳回 CANCELED-撤回 AUTO-自动节点执行） */
    private String action;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long approverId;

    private String approverName;

    /** 审批意见 / 自动节点摘要 */
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
