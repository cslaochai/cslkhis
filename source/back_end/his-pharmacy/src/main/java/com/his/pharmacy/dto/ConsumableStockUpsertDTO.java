package com.his.pharmacy.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 耗材库存建批/入库入参
 */
@Data
public class ConsumableStockUpsertDTO {
    /**
     * 耗材ID（必填，从字典选择）
     */
    @NotNull(message = "耗材ID不能为空，请从耗材字典选择")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    /**
     * 批号（必填，同批号已存在则拒绝，走补货入库）
     */
    @NotBlank(message = "批号不能为空")
    private String batchNo;
    /**
     * 生产日期
     */
    private LocalDate productionDate;
    /**
     * 有效期（必填）
     */
    @NotNull(message = "有效期不能为空")
    private LocalDate expiryDate;
    /**
     * 数量（>0）
     */
    private BigDecimal quantity;
    /**
     * 成本价（>=0）
     */
    private BigDecimal costPrice;
    /**
     * 存放位置
     */
    private String location;
    /**
     * 供应商
     */
    private String supplier;
}
