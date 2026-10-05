package com.his.supplies.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 耗材补货入库/其他出库入参
 */
@Data
public class ConsumableStockChangeDTO {
    /**
     * 库存批次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;
    /**
     * 数量（>0）
     */
    private BigDecimal quantity;
}
