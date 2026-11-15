package com.his.workflow.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 流转线出参。
 */
@Data
public class FlowSkipVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String nowNodeCode;

    private String nextNodeCode;

    private String skipType;

    private String conditionExpr;

    private Integer sortNo;
}
