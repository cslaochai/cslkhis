package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊治疗按次执行流水（治疗执行记录）—— 一次治疗一行。
 */
@Data
@TableName("biz_treatment_record")
public class BizTreatmentRecord implements Serializable {

    /**
     * charge_fail_reason 列宽：写库前必须截断，超长会让整条 update 失败（"记账失败"升级成 500）
     */
    public static final int REASON_MAX = 500;

    /**
     * 治疗记录ID
     */
    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 治疗记录编号
     */
    private String recordNo;

    /**
     * 治疗申请ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 治疗项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long treatmentItemId;

    /**
     * 第几次执行（1 起）
     */
    private Integer execSeq;

    /**
     * 计划执行日期（排期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /**
     * 执行状态（0-待执行 1-已执行 2-已取消）
     */
    private Integer execStatus;

    /**
     * 执行医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executeDoctorId;

    /**
     * 执行护士ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 执行人姓名
     */
    private String executorName;

    /**
     * 执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 记录状态（0-异常 1-正常）
     */
    private Integer recordStatus;

    /**
     * 治疗结果描述
     */
    private String result;

    /**
     * 计费状态（0-未计费 1-已计费 2-计费失败 3-无需计费）
     */
    private Integer chargeStatus;

    /**
     * 计费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chargeTime;

    /**
     * 记账单号
     */
    private String feeNo;

    /**
     * 记账行ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 本次计费金额
     */
    private BigDecimal chargeAmount;

    /**
     * 未计费/失败原因
     */
    private String chargeFailReason;

    /**
     * 备注
     */
    private String remark;
}
