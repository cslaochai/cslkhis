package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 合理用药审查的单据行（处方/医嘱里的一行药品）
 */
@Data
public class DrugRationalItemDTO {

    /**
     * 药品ID（仅用于回显与定位，可空）
     */
    private Long drugId;

    /**
     * 药品名称（单据快照）
     */
    @NotBlank(message = "药品名称不能为空")
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
     * 单次剂量原文（库里是自由文本：'2'、'1片'、'0.5g'）
     */
    private String singleDosage;

    /**
     * 用药频次原文（'一日三次'、'bid'、'隔日一次'）
     */
    private String frequency;
}
