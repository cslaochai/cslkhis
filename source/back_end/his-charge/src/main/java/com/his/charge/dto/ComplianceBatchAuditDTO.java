package com.his.charge.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量合规筛查入参
 */
@Data
public class ComplianceBatchAuditDTO {

    /**
     * 结算清单ID列表
     */
    @NotEmpty(message = "请选择要筛查的结算清单")
    private List<Long> settlementIds;

    /**
     * 审核类型（1-结算前自查 2-批量筛查 3-医保反馈复核）
     */
    private Integer auditType;

    /**
     * 备注
     */
    private String remark;
}
