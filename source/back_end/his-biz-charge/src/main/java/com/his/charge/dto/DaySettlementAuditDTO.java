package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 日结单审核入参。
 */
@Data
public class DaySettlementAuditDTO {

    /**
     * 日结单ID
     */
    @NotNull(message = "日结单ID不能为空")
    private Long id;

    /**
     * 审核意见
     */
    private String auditRemark;

    /**
     * 备注
     */
    private String remark;
}
