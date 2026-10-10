package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者过敏史新增/修改入参
 */
@Data
public class PatientAllergyUpsertDTO {
    /**
     * 过敏史ID，新增时为空，修改时必填
     */
    private Long id;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 过敏类型（如：药物、食物、环境）
     */
    @NotBlank(message = "过敏类型不能为空（药物/食物/其他）")
    private String allergyType;
    /**
     * 过敏原名称
     */
    @NotBlank(message = "过敏原名称不能为空")
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
