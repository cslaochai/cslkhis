package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处方审核入参。
 */
@Data
public class PrescriptionAuditDTO {

    /**
     * 处方ID
     */
    @NotNull(message = "处方不能为空")
    private Long prescriptionId;

    /**
     * 审核结论（1通过 / 2退回）—— L7 审方退回重开闭环。
     *
     * <p>退回：不产生药师签名（签名=签发，拒绝签发不留签名），处方置 7-审方退回，
     * 退回原因必填；通过：走原审方签名链，处方置 3-已审方。
     */
    @NotNull(message = "审核结论不能为空（1通过/2退回）")
    private Integer auditResult;

    /**
     * 审方意见（通过时可为空；**退回时必填**，作为退回原因记入处方与站内信）
     */
    private String auditOpinion;
}
