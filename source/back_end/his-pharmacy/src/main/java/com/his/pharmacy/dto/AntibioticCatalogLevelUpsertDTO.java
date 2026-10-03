package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 维护药品的抗菌药物分级与 DDD 值。
 *
 * <p>纳入目录（level 1/2/3）时 DDD 值与单位含药量必须同时给 —— 没有 DDD 值的抗菌药
 * 进不了使用强度统计，等于"在目录里但算不进指标"，比不标更糟（看起来覆盖了实际没覆盖）。
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
