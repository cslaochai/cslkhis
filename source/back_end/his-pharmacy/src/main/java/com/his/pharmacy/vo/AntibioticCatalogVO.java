package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 抗菌药物分级目录行（药品字典快照）
 */
@Data
public class AntibioticCatalogVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String drugCode;

    /** 药品名称 */
    private String drugName;

    private String genericName;

    private String specification;

    private String dosageForm;

    /** 单位 */
    private String unit;

    private String categoryName;

    /** 抗菌药物分级（0非抗菌 1非限制 2限制 3特殊使用），字典 his_antibiotic_level */
    private Integer antibioticLevel;

    /** 分级文案（非抗菌药物/非限制使用级/限制使用级/特殊使用级） */
    private String antibioticLevelText;

    /** WHO 限定日剂量（g/日） */
    private BigDecimal dddValue;

    /** 每发药单位含有效成分克数 */
    private BigDecimal dddUnitGram;

    /** 是否已纳入抗菌药物管理（antibioticLevel > 0） */
    private Boolean inCatalog;
}
