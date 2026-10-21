package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * FEFO 扣减结果：该药品全部批次合计的扣减前后数量（写回发药记录的配药前/配药后库存）。
 */
@Data
@NoArgsConstructor
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
