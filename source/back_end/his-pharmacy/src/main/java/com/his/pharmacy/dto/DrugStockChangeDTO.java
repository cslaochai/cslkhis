package com.his.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存入库/出库变动入参
 */
@Data
public class DrugStockChangeDTO {
    /** 库存批次ID */
    private Long stockId;
    /**
     * 变动数量（正数），单位：最小包装单位
     */
    private BigDecimal quantity;
}
