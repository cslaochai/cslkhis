package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * LIS 室内质控计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_lis_qc_plan")
public class BizLisQcPlan extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 质控计划编号（唯一）
     */
    private String planNo;

    /**
     * 检验项目ID
     */
    private Long itemId;

    /**
     * 检验项目编码
     */
    private String itemCode;

    /**
     * 检验项目名称
     */
    private String itemName;

    /**
     * 仪器编号
     */
    private String instrumentNo;

    /**
     * 仪器名称
     */
    private String instrumentName;

    /**
     * 质控水平（1-低值 2-中值 3-高值）
     */
    private Integer qcLevel;

    /**
     * 质控品名称
     */
    private String controlName;

    /**
     * 质控品批号
     */
    private String controlLotNo;

    /**
     * 生产厂家
     */
    private String manufacturer;

    /**
     * 靶值（均值）
     */
    private BigDecimal meanValue;

    /**
     * 标准差 SD
     */
    private BigDecimal sdValue;

    /**
     * 允许 CV 上限（%）
     */
    private BigDecimal cvLimit;

    /**
     * 质控品效期
     */
    private LocalDate expireDate;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;
}
