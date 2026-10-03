package com.his.operation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 单一清点明细在某一阶段的数量。
 */
@Data
public class CountQtyDTO implements Serializable {

    @NotNull(message = "清点明细ID不能为空")
    private Long itemId;

    @NotNull(message = "数量不能为空（没数的项也请写 0）")
    @Min(value = 0, message = "数量不能为负")
    private Integer qty;
}
