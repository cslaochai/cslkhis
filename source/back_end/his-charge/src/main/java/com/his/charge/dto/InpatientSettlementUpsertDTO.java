package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 住院出院结算入参。
 */
@Data
public class InpatientSettlementUpsertDTO {

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 结算方式：1-自费 2-医保（不传按患者有无参保号自动判定）
     */
    private Integer settleMode;

    /**
     * 备注（欠费结算建议写清欠费原因）
     */
    private String remark;
}
