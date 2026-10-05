package com.his.supplies.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 耗材科室领用入参（领用即扣库存）
 */
@Data
public class ConsumableConsumeDTO {
    /**
     * 耗材ID
     */
    @NotNull(message = "耗材ID不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    /**
     * 领用数量（>0）
     */
    private BigDecimal quantity;
    /**
     * 领用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 用途
     */
    private String purpose;
}
