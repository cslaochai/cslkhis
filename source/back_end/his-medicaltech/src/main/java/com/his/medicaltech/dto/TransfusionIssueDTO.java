package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 发血入参：输血科把配血相合的血袋发往病区。
 */
@Data
public class TransfusionIssueDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 发血备注（如"已核对血袋外观"）
     */
    private String issueRemark;

    /**
     * 备注
     */
    private String remark;
}
