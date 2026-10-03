package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 处方明细出参
 */
@Data
public class BizPrescriptionDetailVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

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
     * 通用名
     */
    private String genericName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 剂型
     */
    private String dosageForm;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价，单位：元
     */
    private BigDecimal price;

    /**
     * 金额，单位：元
     */
    private BigDecimal amount;

    /**
     * 用法用量
     */
    private String usageDosage;

    /**
     * 缴费状态（0-未缴费 1-已缴费 2-已退费）
     */
    private Integer paymentStatus;

    /**
     * 用药频次
     */
    private String frequency;

    /**
     * 用药途径
     */
    private String route;

    /**
     * 疗程天数
     */
    private Integer duration;

    /**
     * 单次剂量
     */
    private String singleDosage;

    /**
     * 是否需要皮试（0-否 1-是）
     */
    private Integer isSkinTest;

    /**
     * 明细状态（1-正常 2-已发药 3-已退药）
     */
    private Integer detailStatus;
}
