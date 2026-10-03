package com.his.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单新增/修改入参（orderId 为空=新增，非空=修改）
 *
 * ⚠ 金额不从这里收：明细的 amount 与订单的 total_amount 一律由服务端按
 *   数量 × 单价重算，前端传了也不采信（否则前端可以把总额改成任意值）。
 */
@Data
public class PurchaseOrderUpsertDTO {

    /** 订单ID（null=新增） */
    private Long orderId;

    /** 供应商ID */
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    /** 下单时间（空则取服务端当前时间） */
    private LocalDateTime orderTime;

    /** 备注 */
    @Size(max = 500, message = "备注最长 500 位")
    private String remark;

    /** 明细项集合 */
    @NotEmpty(message = "采购明细不能为空，至少要有一条")
    @Valid
    private List<PurchaseOrderDetailUpsertDTO> items;
}
