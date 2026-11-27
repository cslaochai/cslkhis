package com.his.clinical.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 病历更正审批意见入参：科室初审 / 医务终审共用。
 */
@Data
public class EmrCorrectionOpinionDTO {

    /**
     * 更正申请单ID
     */
    @NotNull(message = "申请单不存在")
    private Long id;

    /**
     * 是否通过（true-通过；false-驳回）
     */
    @NotNull(message = "请给出审批结论")
    private Boolean approved;

    /**
     * 审批意见（驳回时必填）
     */
    private String comment;
}
