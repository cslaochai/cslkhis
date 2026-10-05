package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者手术外伤史出参
 */
@Data
public class PatientSurgeryHistoryVO {
    /**
     * 手术外伤史ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 手术/外伤名称
     */
    private String surgeryName;
    /**
     * 手术/外伤日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate surgeryDate;
    /**
     * 手术类型（如：门诊、住院）
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
