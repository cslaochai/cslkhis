package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采购订单「生成入库单」入参
 *
 * 语义提醒：这一步**不动库存**，只是把采购单转成一张待审核的入库单；
 * 真正的入库（建/加药品批次 + 写库存流水）发生在入库单上（/drugInbound/stockIn）。
 */
@Data
public class PurchaseInboundDTO {

    /** 采购订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
