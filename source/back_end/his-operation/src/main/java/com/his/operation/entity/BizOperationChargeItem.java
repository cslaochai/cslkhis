package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 手术麻醉计费明细（手术麻醉计费明细）—— 计费联动的唯一证据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_operation_charge_item")
public class BizOperationChargeItem extends BaseEntity {

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 手术申请单号
     */
    private String applyNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 收费来源（预留）（1-麻醉记录 2-PACU复苏 3-手术）
     */
    private Integer sourceType;

    /**
     * 来源单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    /**
     * 来源单据号
     */
    private String sourceNo;

    /**
     * 收费项目编码
     */
    private String itemCode;

    /**
     * 收费项目名称
     */
    private String itemName;

    /**
     * 费用记账流水的项目类型（7-治疗）
     */
    private Integer itemType;

    /**
     * 规格
     */
    private String spec;

    /**
     * 计价单位
     */
    private String unit;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 计费状态（0-未计费 1-已计费 2-计费失败）
     */
    private Integer chargeStatus;

    /**
     * 记账行ID（费用记账流水的ID，本项目落账的那一行）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 记账单号（费用记账流水的费用编号）
     */
    private String feeNo;

    /**
     * 失败原因（未发现项目 / 金额异常 / 收费模块缺席）
     */
    private String failReason;
}
