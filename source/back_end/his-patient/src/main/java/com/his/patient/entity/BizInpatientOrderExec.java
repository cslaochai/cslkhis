package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 住院医嘱执行记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_order_exec")
public class BizInpatientOrderExec extends BaseEntity implements Serializable {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 医嘱ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /**
     * 入院ID（冗余，便于按住院查询）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 本条医嘱的第几次执行（从 1 开始）
     */
    private Integer execSeq;

    /**
     * 计划日期（"同医嘱同日只生成一条计划"的幂等判据）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /**
     * 计划执行时间（待执行队列按它升序）
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
     * 执行备注 / 跳过原因（跳过必填）
     */
    private String execNote;

    /**
     * 输液开始时间（输液闭环：执行后开始输注时写入，G14）
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
     * 不良反应描述（adverseFlag=1 必填）
     */
    private String adverseNote;

    /**
     * 本次执行生成的记账行ID（费用记账流水的ID；未记账为 NULL）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 本次执行生成的记账单号（费用记账流水的费用编号）
     */
    private String feeNo;
}
