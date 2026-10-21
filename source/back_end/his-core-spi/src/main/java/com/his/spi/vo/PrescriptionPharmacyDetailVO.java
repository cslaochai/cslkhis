package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 药房域读取的处方明细只读快照（事实宿主：临床域 biz_prescription_detail）。
 */
@Data
public class PrescriptionPharmacyDetailVO implements Serializable {

    /**
     * 处方明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 剂型（限量档位判定用）
     */
    private String dosageForm;

    /**
     * 数量（中药为克数）
     */
    private BigDecimal quantity;

    /**
     * 单次剂量文本
     */
    private String singleDosage;

    /**
     * 用药频次（中文自由文本）
     */
    private String frequency;

    /**
     * 给药途径 / 煎法脚注（先煎/后下等）
     */
    private String route;

    /**
     * 疗程天数
     */
    private Integer duration;
}
