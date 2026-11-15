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
 * 流程定义（sys_flow_definition）：一条流程的元信息，节点与流转线另表存放。
 */
@Data
@TableName("sys_flow_definition")
public class FlowDefinition {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 流程编码，全库唯一 */
    private String flowCode;

    /** 流程名称 */
    private String flowName;

    /** 流程说明（意图路由时给模型看的边界描述） */
    private String flowDesc;

    /** 状态（0-草稿 1-已发布 2-已停用），见 FlowDefinitionStatusEnum */
    private Integer status;

    /** 版本号，发布时递增 */
    private Integer version;

    /** 整图快照 JSON，仅用于回看与比对，不参与执行 */
    private String modelJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
