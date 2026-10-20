package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合理用药审查的单据行：处方或医嘱里的一行药品，名称与规格取单据快照。
 */
@Data
@NoArgsConstructor
public class DrugRationalCheckItemDTO {

    /**
     * 药品ID（仅用于回显与定位，可空）
     */
    private Long drugId;

    /**
     * 药品名称（单据快照）
     */
    private String drugName;

    /**
     * 通用名（单据快照，可空）
     */
    private String genericName;

    /**
     * 规格（单据快照，剂量上限比较要用它换算单件含量）
     */
    private String specification;

    /**
     * 单次剂量原文
     */
    private String singleDosage;

    /**
     * 用药频次原文
     */
    private String frequency;
}
