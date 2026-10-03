package com.his.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * FEFO 扣减结果：该药品全部批次合计的前后数量（写回发药记录 stock_before/stock_after）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductResultDTO {
    /**
     * 扣减前该药品全部批次合计数量
     */
    private BigDecimal quantityBefore;
    /**
     * 扣减后该药品全部批次合计数量
     */
    private BigDecimal quantityAfter;
}
