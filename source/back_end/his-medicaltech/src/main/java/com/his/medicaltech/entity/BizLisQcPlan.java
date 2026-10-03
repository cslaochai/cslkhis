package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LIS 室内质控计划
 *
 * <p>一条计划 = 一个（检验项目 × 仪器 × 质控水平）组合的靶值与 SD。
 * 靶值/SD 是判定的基准，必须由计划提供 —— 没有靶值就没法算 Z，
 * 也就谈不上在控/失控。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_lis_qc_plan")
public class BizLisQcPlan extends BaseEntity {

    /** 质控计划编号（唯一） */
    private String planNo;

    /** 检验项目ID */
    private Long itemId;

    /** 检验项目编码 */
    private String itemCode;

    /** 检验项目名称 */
    private String itemName;

    /** 仪器编号 */
    private String instrumentNo;

    /** 仪器名称 */
    private String instrumentName;

    /** 质控水平（1-低值 2-中值 3-高值） */
    private Integer qcLevel;

    /** 质控品名称 */
    private String controlName;

    /** 质控品批号 */
    private String controlLotNo;

    /** 生产厂家 */
    private String manufacturer;

    /** 靶值（均值） */
    private BigDecimal meanValue;

    /** 标准差 SD */
    private BigDecimal sdValue;

    /** 允许 CV 上限（%） */
    private BigDecimal cvLimit;

    /** 质控品效期 */
    private LocalDate expireDate;

    /** 状态（1-启用 0-停用） */
    private Integer status;
}
