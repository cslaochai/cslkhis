package com.his.workflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 审批待办处理（通过/驳回共用）。
 */
@Data
public class FlowTaskHandleDTO implements Serializable {

    @NotNull(message = "待办ID不能为空")
    private Long taskId;

    /** 审批意见 */
    private String message;
}
