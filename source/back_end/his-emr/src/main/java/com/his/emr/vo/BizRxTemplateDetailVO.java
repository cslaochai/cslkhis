package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 处方模板明细出参
 */
@Data
public class BizRxTemplateDetailVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 所属处方模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

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
