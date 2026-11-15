package com.his.workflow.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程节点出参。
 */
@Data
public class FlowNodeVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String nodeCode;

    private String nodeName;

    private Integer nodeType;

    private String nodeTypeLabel;

    private String permissionFlag;

    private String nodeRatio;

    private String aiAction;

    private String aiCapabilityKey;

    private String aiTemplateName;

    private String aiInputJson;

    private String coordinate;

    private Integer sortNo;
}
