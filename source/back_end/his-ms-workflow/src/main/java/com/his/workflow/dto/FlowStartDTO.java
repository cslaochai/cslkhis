package com.his.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 启动流程。
 */
@Data
public class FlowStartDTO implements Serializable {

    @NotBlank(message = "流程编码不能为空")
    private String flowCode;

    /** 业务单据 ID，可空（纯咨询类流程没有单据） */
    private String businessId;

    /** 业务类型，审计与检索用 */
    private String bizType;

    /** 入口变量 JSON，作为流程变量初值 */
    private String variableJson;

    /** 发起人，内部调用必填；web 入口留空则取当前登录人 */
    private Long startedByUserId;

    private String startedByName;
}
