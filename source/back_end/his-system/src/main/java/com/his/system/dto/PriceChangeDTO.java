package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 调价入参
 */
@Data
public class PriceChangeDTO {

    /**
     * 项目类型：DRUG-药品 CONSUMABLE-耗材 INSPECTION-检查 LABORATORY-检验 TREATMENT-治疗
     */
    @NotBlank(message = "项目类型不能为空")
    private String itemType;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long itemId;

    /**
     * 新价格
     */
    @NotNull(message = "新价格不能为空")
    private BigDecimal newPrice;

    /**
     * 调价原因（必填，进调价留痕）
     */
    @NotBlank(message = "调价原因不能为空")
    private String reason;
}
