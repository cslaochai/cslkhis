package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采购订单「生成入库单」入参
 */
@Data
public class PurchaseInboundDTO {

    /** 采购订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
