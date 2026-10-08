package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用记账行（L1 应收的唯一来源）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_fee_record")
public class BizFeeRecord extends BaseEntity {

    /**
     * 记账流水号
     */
    private String feeNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 就诊类型（1-门诊 2-住院），字典 {@code his_encounter_type}。
     * 不复用 visit_type（那是初诊/复诊）。
     */
    private Integer encounterType;

    /**
     * 就诊标识：门诊=挂号信息的ID；住院=入院记录的入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 就诊标识单号
     */
    private String encounterNo;

    /**
     * 费用归属科室（收入统计与科室对账口径）。取不到就是 NULL，
     * 日结会把它单列成「无科室归属」差异项，不许回填成任意科室。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 开单/执行人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 开单人姓名
     */
    private String doctorName;

    /**
     * 项目类型，字典 {@code his_charge_item_type}（1-挂号费 … 8-耗材）
     */
    private Integer itemType;

    /**
     * 项目/药品编码
     */
    private String itemCode;

    /**
     * 项目名称：记账时快照，字典改名不影响历史账单
     */
    private String itemName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）：结算层算统筹/自付的依据。
     * 记账时快照，目录调类不能改写历史应收的报销依据。
     */
    private Integer catalogType;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 红冲行为负的冲减数量
     */
    private BigDecimal quantity;

    /**
     * 金额 = 单价 × 数量（红冲行为负）
     */
    private BigDecimal amount;

    /**
     * 记账状态，字典 {@code his_fee_status}：1-待结算 2-已锁定 3-已结算 4-已红冲
     */
    private Integer feeStatus;

    /**
     * 费用来源单据，字典 {@code his_fee_source_type}
     */
    private Integer sourceType;

    /**
     * 来源单据ID（处方明细ID/申请ID/医嘱ID…）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    /**
     * 来源单据号
     */
    private String sourceNo;

    /**
     * 红冲指针：负行指向被冲的原行（一行一个，恒有值）。
     *
     * <p>原行只在<b>整行冲完</b>时反向指一次（指向最后一笔负行）：部分冲减可以有多笔负行，
     * 一列存不下多对一，硬填只会指错人；反查用 orig_fee_id = 原行ID 就能拿到全链。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origFeeId;

    /**
     * 累计已冲金额（每执行一笔红冲就累加 abs(负行amount)，用于快速判断剩余可退额）
     */
    private BigDecimal refundedAmount;

    /**
     * 记账时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookTime;

    /**
     * 记账人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bookById;

    /**
     * 记账人姓名
     */
    private String bookByName;

    /**
     * 所属结算账单ID：NULL=尚未结算。进账单时回填，是"这条费用被谁锁定"的唯一答案。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;
}
