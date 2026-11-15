package com.his.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 流转线新增/修改。
 */
@Data
public class FlowSkipUpsertDTO implements Serializable {

    @NotBlank(message = "起点节点不能为空")
    private String nowNodeCode;

    @NotBlank(message = "目标节点不能为空")
    private String nextNodeCode;

    /** 流转线类型（PASS/COND/REJECT） */
    @NotBlank(message = "流转线类型不能为空")
    private String skipType;

    /** 条件表达式，仅 COND 使用；DEFAULT 表示兜底分支 */
    private String conditionExpr;

    private Integer sortNo;
}
