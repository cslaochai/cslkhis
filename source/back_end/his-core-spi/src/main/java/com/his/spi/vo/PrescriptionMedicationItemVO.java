package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 处方用药快照明细：一名药一行，只带服用方法能读出来的列。
 */
@Data
@NoArgsConstructor
public class PrescriptionMedicationItemVO {

    /**
     * 处方明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 药品ID（用于反查药品档案的储存、特殊管制等客观属性）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品编码（drugId 缺失时的回退口径）
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
     * 剂型
     */
    private String dosageForm;

    /**
     * 数量（中药为每味克数）
     */
    private BigDecimal quantity;

    /**
     * 数量单位
     */
    private String unit;

    /**
     * 单次剂量
     */
    private String singleDosage;

    /**
     * 用法用量原句
     */
    private String usageDosage;

    /**
     * 频次
     */
    private String frequency;

    /**
     * 给药途径
     */
    private String route;

    /**
     * 疗程天数
     */
    private Integer duration;

    /**
     * 是否皮试（1-是 0-否）
     */
    private Integer isSkinTest;
}
