package com.his.medicaltech.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用血分级审批提交 DTO（sql/93）。
 */
@Data
public class TransfusionApproveDTO {

    /**
     * 输血申请单ID
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 审批结论（1-通过 2-驳回）
     */
    @NotNull(message = "审批结论不能为空")
    @Min(value = 1, message = "审批结论取值不合法（1-通过 2-驳回）")
    @Max(value = 2, message = "审批结论取值不合法（1-通过 2-驳回）")
    private Integer approveResult;

    /**
     * 审批意见（驳回时必填原因）
     */
    private String opinion;
}
