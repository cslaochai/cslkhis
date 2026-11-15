package com.his.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程定义详情（列表接口 nodes/skips 为空）。
 */
@Data
public class FlowDefinitionVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String flowCode;

    private String flowName;

    private String flowDesc;

    private Integer status;

    private String statusLabel;

    private Integer version;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private List<FlowNodeVO> nodes;

    private List<FlowSkipVO> skips;
}
