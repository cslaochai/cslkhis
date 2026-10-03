package com.his.fee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 记账入参（L1）。
 *
 * <p>金额不在入参里：金额一律由服务端按单价 × 数量现算，
 * 信前端传来的金额等于把应收交给调用方定义。
 */
@Data
public class FeeBookDTO {

    /**
     * 记账行ID：仅"补记一行已红冲费用"这类内部场景使用，HTTP 入口一律为空
     */
    private Long id;

    /** 患者ID */
    @NotNull(message = "缺少患者")
    private Long patientId;

    /** 患者号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    @NotBlank(message = "缺少患者姓名")
    private String patientName;

    /**
     * 就诊类型（字典 his_encounter_type：1-门诊 2-住院）
     */
    @NotNull(message = "缺少就诊类型")
    private Integer encounterType;

    /**
     * 就诊标识：门诊=挂号ID，住院=入院ID
     */
    @NotNull(message = "缺少就诊标识")
    private Long encounterId;

    /** 就诊标识单号 */
    private String encounterNo;

    /**
     * 费用归属科室（收入统计口径）。允许为空：取不到就是"无科室归属"，
     * 由日结单列成差异项，比回填成任意科室诚实。
     */
    private Long deptId;

    /** 科室名称（快照） */
    private String deptName;

    /** 开单/执行人员工ID */
    private Long doctorId;

    /** 开单人姓名（快照） */
    private String doctorName;

    /** 项目类型（1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材） */
    @NotNull(message = "缺少项目类型")
    private Integer itemType;

    /** 项目/药品编码 */
    private String itemCode;

    /** 项目名称 */
    @NotBlank(message = "缺少项目名称")
    private String itemName;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 单价 */
    @NotNull(message = "缺少单价")
    @DecimalMin(value = "0", message = "单价不能为负")
    private BigDecimal price;

    /**
     * 数量：必须为正，冲减走红冲接口而不是传负数
     */
    @NotNull(message = "缺少数量")
    @DecimalMin(value = "0", message = "数量不能为负")
    private BigDecimal quantity;

    /**
     * 费用来源单据（字典 his_fee_source_type）
     */
    @NotNull(message = "缺少费用来源")
    private Integer sourceType;

    /**
     * 来源单据ID：记账幂等靠 sourceType + sourceId + itemCode 判重，
     * 同一张处方重复执行不能记两次费用。
     */
    private Long sourceId;

    /** 来源单据号 */
    private String sourceNo;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类），决定结算层的报销分摊；不传按自费
     */
    private Integer catalogType;

    /** 备注 */
    private String remark;
}
