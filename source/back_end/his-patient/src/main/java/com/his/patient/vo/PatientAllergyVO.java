package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者过敏史出参
 */
@Data
public class PatientAllergyVO {
    /**
     * 过敏史ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 过敏类型（如：药物、食物、环境）
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
     * 过敏症状描述
     */
    private String allergySymptoms;
    /**
     * 过敏发生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate allergyDate;
    /**
     * 发生次数
     */
    private Integer occurrenceCount;
    /**
     * 处理/治疗措施
     */
    private String treatmentGiven;
    /**
     * 确认人
     */
    private String confirmedBy;
}
