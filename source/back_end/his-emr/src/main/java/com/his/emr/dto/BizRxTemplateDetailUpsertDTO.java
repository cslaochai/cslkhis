package com.his.emr.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 处方模板明细入参
 */
@Data
public class BizRxTemplateDetailUpsertDTO {
    /**
     * 明细ID，新增时为空
     */
    private Long id;

    /**
     * 所属处方模板ID
     */
    private Long templateId;

    /**
     * 药品ID
     */
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
     * 药品通用名
     */
    private String genericName;

    /**
     * 规格（如 0.25g*24粒）
     */
    private String specification;

    /**
     * 剂型（如片剂、注射液）
     */
    private String dosageForm;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 单位（如盒、支、片）
     */
    private String unit;

    /**
     * 数量（开药总量）
     */
    private BigDecimal quantity;

    /**
     * 单价，单位：元
     */
    private BigDecimal price;

    /**
     * 金额（数量×单价），单位：元
     */
    private BigDecimal amount;

    /**
     * 用法用量
     */
    private String usageDosage;

    /**
     * 用药频次（如一日三次）
     */
    private String frequency;

    /**
     * 给药途径（如口服、静脉滴注）
     */
    private String route;

    /**
     * 用药天数
     */
    private Integer duration;

    /**
     * 单次用量（如 1片、10ml）
     */
    private String singleDosage;
}
