package com.his.supplies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 高值耗材使用溯源台账（一物一行：扫码→关联患者→扣批次1件→计费留痕）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_consumable_trace")
public class BizConsumableTrace extends BaseEntity {
    /** 院内追溯码（HV+时间戳） */
    private String traceNo;
    /** UDI 原文（扫码枪整串留底） */
    private String udiCode;
    /** 解析-产品标识（GS1 (01)） */
    private String udiDi;
    /** 解析-序列号（GS1 (21)） */
    private String udiSerial;
    /** 解析-批号（GS1 (10)） */
    private String udiBatch;
    /** 解析-有效期（GS1 (17)） */
    private LocalDate udiExpiryDate;
    /** 耗材ID（耗材字典的ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    /** 耗材编码（快照） */
    private String consumableCode;
    /** 耗材名称（快照） */
    private String consumableName;
    /** 规格（快照） */
    private String specification;
    /** 单位（快照） */
    private String unit;
    /** 注册证号（快照） */
    private String regCertNo;
    /** 计费单价快照（登记时字典零售价） */
    private BigDecimal retailPrice;
    /** 出库批次ID（耗材批次库存的ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;
    /** 批号（快照） */
    private String batchNo;
    /** 供应商（快照） */
    private String supplier;
    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /** 患者编号（快照） */
    private String patientNo;
    /** 患者姓名（快照） */
    private String patientName;
    /** 就诊类型（1-门诊 2-住院） */
    private Integer visitType;
    /** 门诊挂号ID（门诊计费锚点） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;
    /** 住院ID（住院计费锚点） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    /** 使用科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /** 使用科室名称（快照） */
    private String deptName;
    /** 使用时间 */
    private LocalDateTime usageTime;
    /** 登记人 */
    private String operatorName;
    /** 计费状态（0-未计费 1-已计费 2-计费失败） */
    private Integer chargeStatus;
    /** 记账单号（费用记账流水的费用编号） */
    private String feeNo;
    /** 记账行ID（费用记账流水的ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;
    /** 未计费/计费失败原因 */
    private String chargeFailReason;
    /** 记录状态（1-使用中 2-已作废/退货） */
    private Integer status;
    /** 作废时间 */
    private LocalDateTime voidTime;
    /** 作废原因 */
    private String voidReason;
}
