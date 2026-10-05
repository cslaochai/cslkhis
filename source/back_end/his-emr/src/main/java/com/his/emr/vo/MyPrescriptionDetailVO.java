package com.his.emr.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 患者端处方明细（只给用药指导相关字段，单价/金额走费用明细页）
 */
@Data
public class MyPrescriptionDetailVO {

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

    private BigDecimal quantity;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单次剂量
     */
    private String singleDosage;

    private String frequency;

    private String route;

    /**
     * 用法用量
     */
    private String usageDosage;

    /**
     * 疗程天数
     */
    private Integer duration;
}
