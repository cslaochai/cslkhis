package com.his.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程实例出参。
 */
@Data
public class FlowInstanceVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String flowCode;

    private String flowName;

    private String businessId;

    private String bizType;

    private Long startedByUserId;

    private String startedByName;

    private String nodeCode;

    private String nodeName;

    private Integer nodeType;

    private String flowStatus;

    private String statusLabel;

    /** 流程变量 JSON，前端按节点编码取各步输出 */
    private String variableJson;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
