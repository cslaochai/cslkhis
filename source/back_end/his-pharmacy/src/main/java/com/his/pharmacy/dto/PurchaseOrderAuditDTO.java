package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 采购订单审批入参
 */
@Data
public class PurchaseOrderAuditDTO {

    /** 采购订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 审批结论（1-通过 2-驳回；0-待审批不是可提交的结论） */
    @NotNull(message = "审批结论不能为空")
    @Min(value = 1, message = "审批结论取值不合法（1-通过 2-驳回）")
    @Max(value = 2, message = "审批结论取值不合法（1-通过 2-驳回）")
    private Integer approvalStatus;

    /** 备注 */
    @Size(max = 500, message = "审批意见最长 500 位")
    private String remark;
}
