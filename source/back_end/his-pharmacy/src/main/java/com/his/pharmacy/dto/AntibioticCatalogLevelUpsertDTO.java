package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 维护药品的抗菌药物分级与 DDD 值。
 */
@Data
public class AntibioticCatalogLevelUpsertDTO {

    @NotNull(message = "药品ID不能为空")
    private Long id;

    /** 抗菌药物分级（0-非抗菌药物 1-非限制 2-限制 3-特殊使用） */
    @NotNull(message = "抗菌药物分级不能为空")
    @Min(value = 0, message = "分级非法")
    @Max(value = 3, message = "分级非法")
    private Integer antibioticLevel;

    /** WHO 限定日剂量（g/日）；level>0 必填 */
    private BigDecimal dddValue;

    /** 每发药单位（盒/瓶/支）含有效成分克数；level>0 必填 */
    private BigDecimal dddUnitGram;
}
