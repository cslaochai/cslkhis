package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院摆药明细 VO。
 */
@Data
public class WardDispenseItemVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 摆药单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispenseId;

    /** 摆药单号（冗余） */
    private String dispenseNo;

    /** 摆药日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDate;

    /** 住院医嘱ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 医嘱号（快照） */
    private String orderNo;

    /** 入院ID（冗余） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID（冗余） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 病区ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品名称（快照） */
    private String drugName;

    /** 医嘱项目编码（快照） */
    private String itemCode;

    /** 医嘱项目名称（快照） */
    private String itemName;

    /** 规格（快照） */
    private String spec;

    /** 单位（快照） */
    private String unit;

    /** 摆药数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal price;

    /** 金额 = quantity × price */
    private BigDecimal amount;

    /**
     * 明细状态：1-待配药 2-已配药 3-已核对 4-已退药
     */
    private Integer status;

    /** 配药前库存 */
    private BigDecimal stockBefore;

    /** 配药后库存 */
    private BigDecimal stockAfter;

    /** 记账行ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /** 记账单号 */
    private String feeNo;

    /** 配药人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispenserId;

    /** 配药人姓名 */
    private String dispenserName;

    /** 配药时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispenseTime;

    /** 核对人ID（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long checkerId;

    /** 核对人姓名 */
    private String checkerName;

    /** 核对时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    /** 退药操作人 */
    private String returnBy;

    /** 退药时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /** 退药原因（必填） */
    private String returnReason;
}
