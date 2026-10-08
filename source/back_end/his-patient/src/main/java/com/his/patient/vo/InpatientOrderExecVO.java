package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医嘱执行记录出参（待执行队列与执行记录共用）。
 */
@Data
public class InpatientOrderExecVO implements Serializable {

    /**
     * 执行记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 医嘱ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /**
     * 医嘱号
     */
    private String orderNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 开立医生姓名
     */
    private String doctorName;

    /**
     * 医嘱类型：1-长期 2-临时
     */
    private Integer orderType;

    /**
     * 医嘱类型文案
     */
    private String orderTypeText;

    /**
     * 医嘱类别
     */
    private Integer orderClass;

    /**
     * 医嘱类别文案
     */
    private String orderClassText;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单次剂量
     */
    private BigDecimal dosage;

    /**
     * 剂量单位
     */
    private String dosageUnit;

    /**
     * 给药途径
     */
    private String route;

    /**
     * 频次
     */
    private String frequency;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价（元）
     */
    private BigDecimal price;

    /**
     * 金额（元）
     */
    private BigDecimal amount;

    /**
     * 是否加急：0-否 1-是
     */
    private Integer isUrgent;

    /**
     * 医嘱状态
     */
    private Integer orderStatus;

    /**
     * 医嘱状态文案
     */
    private String orderStatusText;

    /**
     * 本条医嘱的第几次执行
     */
    private Integer execSeq;

    /**
     * 计划日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /**
     * 计划执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planTime;

    /**
     * 实际执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime execTime;

    /**
     * 执行护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long execNurseId;

    /**
     * 执行护士姓名
     */
    private String execNurseName;

    /**
     * 执行状态：1-待执行 2-已执行 3-已跳过 4-已退回
     */
    private Integer execStatus;

    /**
     * 执行状态文案
     */
    private String execStatusText;

    /**
     * 执行备注 / 跳过原因
     */
    private String execNote;

    /**
     * 输液开始时间（输液闭环 G14）
     */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime infusionStartTime;

    /**
     * 开始滴速（滴/分）
     */
    private Integer dripRate;

    /**
     * 输液结束时间
     */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime infusionEndTime;

    /**
     * 输液不良反应：0-无 1-有
     */
    private Integer adverseFlag;

    /**
     * 不良反应描述
     */
    private String adverseNote;

    /**
     * 给药途径是否静脉类（输液闭环入口判定，后端算好给前端，口径单点）
     */
    private Boolean infusion;

    /**
     * 记账行ID（费用记账流水的ID；未记账为 null）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 记账单号（费用记账流水的费用编号）
     */
    private String feeNo;

    /**
     * 是否已计费（由 feeRecordId 派生，供护士站直观判断）
     */
    private Boolean charged;
}
