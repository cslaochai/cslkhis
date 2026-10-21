package com.his.clinical.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDate;

/**
 * 病历三级质控流转单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_record_qc_flow")
public class BizRecordQcFlow extends BaseEntity {

    /**
     * 流转单号 QCF+yyyyMMdd+4位
     */
    private String flowNo;

    /**
     * 病历ID 门诊病历的ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历来源（OUTPATIENT-门诊 INPATIENT-住院）
     */
    private String recordSource;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（发起时快照）
     */
    private String patientName;

    /**
     * 病历所属科室ID（科级审核归口）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 病历所属科室名称
     */
    private String deptName;

    /**
     * 流转状态（1-科级待审 2-病案室待审 3-医务处待审 4-终审通过 5-整改中）
     */
    private Integer flowStatus;

    /**
     * 当前停留级（1科级 2病案室 3医务处；整改中=需回到的级）
     */
    private Integer currentLevel;

    /**
     * 最近一次退回发生级（1/2/3）
     */
    private Integer returnLevel;

    /**
     * 最近一次退回的缺陷明细
     */
    private String returnReason;

    /**
     * 最近一次退回的整改要求
     */
    private String returnRequirement;

    /**
     * 整改期限
     */
    private LocalDate returnDeadline;

    /**
     * 终审定级（1-甲级 2-乙级 3-丙级）
     */
    private Integer grade;

    /**
     * 终审评分（0-100）
     */
    private Integer finalScore;

    /**
     * 终审意见
     */
    private String finalOpinion;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
