package com.his.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 实盘数录入入参（整批提交，未出现在 items 里的明细保持「未录入」）
 */
@Data
public class StocktakeCountUpsertDTO {

    @NotNull(message = "盘点单ID不能为空")
    private Long id;

    /** 明细项集合 */
    @NotEmpty(message = "实盘明细不能为空")
    @Valid
    private List<Item> items;

    /**
     * 单条实盘数
     */
    @Data
    public static class Item {

        @NotNull(message = "盘点明细ID不能为空")
        private Long itemId;

        /** 实盘数量（0 是合法值：账上有货、实物没有就是盘亏） */
        @NotNull(message = "实盘数量不能为空")
        @DecimalMin(value = "0", message = "实盘数量不能为负数")
        private BigDecimal countedQuantity;

        /** 差异说明（服务端截到列宽 500） */
        private String remark;
    }
}
