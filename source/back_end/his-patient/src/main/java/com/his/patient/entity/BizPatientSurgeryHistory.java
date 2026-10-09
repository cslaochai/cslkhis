package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 手术外伤史
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_surgery_history")
public class BizPatientSurgeryHistory extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 手术名称
     */
    private String surgeryName;
    /**
     * 手术日期
     */
    private LocalDate surgeryDate;
    /**
     * 手术类型（择期/紧急/急诊）
     */
    private String surgeryType;
    /**
     * 主刀医生
     */
    private String surgeon;
    /**
     * 麻醉方式
     */
    private String anesthesiaType;
    /**
     * 手术医院
     */
    private String hospitalName;
    /**
     * 术后诊断
     */
    private String postopDiagnosis;
    /**
     * 恢复情况（良好/一般/差/死亡）
     */
    private String recoveryStatus;
    /**
     * 术后并发症
     */
    private String complications;
}
