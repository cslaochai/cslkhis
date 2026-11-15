package com.his.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办出参。
 */
@Data
public class FlowTaskVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long instanceId;

    private String flowCode;

    private String flowName;

    private String businessId;

    private String nodeCode;

    private String nodeName;

    private Integer nodeType;

    private String taskStatus;

    private String statusLabel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
