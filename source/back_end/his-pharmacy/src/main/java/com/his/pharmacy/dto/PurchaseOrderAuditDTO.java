package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 采购订单审批入参
 *
 * 结论只允许 1（通过）/ 2（驳回）；「回到待审批」没有接口 —— 状态只能向前走，
 * 需要改单就先驳回（2），修改后重新提交会回到待审批。
 */
@Data
public class PurchaseOrderAuditDTO {

    /** 采购订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 审批状态（0-待审批 1-已通过 2-已驳回） */
    @NotNull(message = "审批结论不能为空")
    private Integer approvalStatus;

    /** 备注 */
    @Size(max = 500, message = "审批意见最长 500 位")
    private String remark;
}
