package com.his.charge.vo;

import com.his.charge.api.MedicalTechGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 检验记录跨域摘要。
 *
 * @see MedicalTechGateway
 */
@Data
@NoArgsConstructor
public class LaboratoryRecordBriefVO {

    /**
     * 检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 检验项目名
     */
    private String laboratoryItemName;

    /**
     * 检验诊断/结论
     */
    private String diagnosis;

    /**
     * 处理建议（报告建议栏）
     */
    private String suggestions;

    /**
     * 就诊日期（跨域查询按「患者 + 就诊日」定位，不按创建时间）
     */
    private LocalDate visitDate;
}
