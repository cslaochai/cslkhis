package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 仪器间比对 / 室间差（室间质评仪器间比对）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_lis_eqa_compare")
public class BizLisEqaCompare extends BaseEntity {

    /**
     * 质评批次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;

    /**
     * 质评批次号（冗余）
     */
    private String planNo;

    /**
     * 检验项目编码
     */
    private String itemCode;

    /**
     * 检验项目名称
     */
    private String itemName;

    /**
     * 第几个样品
     */
    private Integer sampleSeq;

    /**
     * 盲样编号
     */
    private String sampleNo;

    /**
     * A 组仪器
     */
    private String instrumentA;

    /**
     * A 组方法学
     */
    private String methodA;

    /**
     * A 组测定值
     */
    private BigDecimal valueA;

    /**
     * B 组仪器
     */
    private String instrumentB;

    /**
     * B 组方法学
     */
    private String methodB;

    /**
     * B 组测定值
     */
    private BigDecimal valueB;

    /**
     * 互差绝对值 |A-B|
     */
    private BigDecimal diffValue;

    /**
     * 相对互差% = |A-B| / ((A+B)/2) × 100
     */
    private BigDecimal diffRate;

    /**
     * 允许互差%
     */
    private BigDecimal allowRate;

    /**
     * 1-由该行 TEa 折半得来 2-无 TEa，取服务端默认 8%
     */
    private Integer allowSource;

    /**
     * 比对结论（1-可接受 2-超差）
     */
    private Integer status;
}
