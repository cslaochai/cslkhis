package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编码任务审核入参（通过 / 退修）
 */
@Data
public class CodeTaskAuditDTO {

    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long id;

    /**
     * true 通过 / false 退修
     */
    @NotNull(message = "请选择审核结论")
    private Boolean approve;

    /**
     * 审核意见（退修必填）
     */
    private String remark;
}
