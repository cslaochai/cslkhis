package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 日结单审核入参。
 *
 * <p>只做"审核通过"这一件事 —— 不做驳回：日结单本身就是汇总结果，
 * 数字不对应当重算（{@code runDaySettlement} 覆盖待审核的草稿），而不是留一张被驳回的单。
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
