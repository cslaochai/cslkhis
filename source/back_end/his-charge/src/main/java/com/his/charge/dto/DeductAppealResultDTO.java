package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 录入申诉结果（仅「申诉中」可录；成功→撤销扣款，驳回→转入待缴）。
 */
@Data
public class DeductAppealResultDTO {

    @NotNull(message = "扣款通知ID不能为空")
    private Long id;

    /**
     * 申诉结果（1-成功 2-驳回）
     */
    @NotNull(message = "申诉结果不能为空")
    private Integer appealResult;

    /**
     * 申诉结果说明（医保局回复原文）
     */
    private String appealResultRemark;
}
