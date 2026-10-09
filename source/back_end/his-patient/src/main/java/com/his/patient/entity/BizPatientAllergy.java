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
 * 药物过敏史
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_allergy")
public class BizPatientAllergy extends BaseEntity {
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
     * 过敏类型（药物/食物/其他）
     */
    private String allergyType;
    /**
     * 过敏原名称
     */
    private String allergenName;
    /**
     * 过敏严重程度（轻度/中度/重度/危及生命）
     */
    private String allergySeverity;
    /**
     * 过敏反应表现
     */
    private String allergySymptoms;
    /**
     * 首次发生日期
     */
    private LocalDate allergyDate;
    /**
     * 发生次数
     */
    private Integer occurrenceCount;
    /**
     * 过敏时处理措施
     */
    private String treatmentGiven;
    /**
     * 确认医生
     */
    private String confirmedBy;
}
