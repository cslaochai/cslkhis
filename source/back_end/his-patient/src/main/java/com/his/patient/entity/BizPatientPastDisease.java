package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 既往疾病史 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_past_disease")
public class BizPatientPastDisease extends BaseEntity {

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 疾病名称
     */
    private String diseaseName;

    /**
     * 疾病编码（ICD-10）
     */
    private String diseaseCode;

    /**
     * 诊断日期
     */
    private LocalDate diagnosisDate;

    /**
     * 诊断科室
     */
    private String diagnosisDept;

    /**
     * 治疗方案
     */
    private String treatmentPlan;

    /**
     * 当前控制情况（已治愈/控制良好/未控制/随访中）
     */
    private String currentStatus;

    /**
     * 复发次数
     */
    private Integer relapseCount;

    /**
     * 最近随访日期
     */
    private LocalDate lastFollowupDate;
}
