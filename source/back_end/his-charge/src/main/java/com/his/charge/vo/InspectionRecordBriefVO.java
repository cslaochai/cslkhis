package com.his.charge.vo;

import com.his.charge.api.MedicalTechGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 检查记录跨域摘要。
 *
 * @see MedicalTechGateway
 */
@Data
@NoArgsConstructor
public class InspectionRecordBriefVO {

    /**
     * 检查记录ID
     */
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 检查项目名
     */
    private String inspectionItemName;

    /**
     * 临床诊断（检查所见/印象）
     */
    private String clinicalDiagnosis;

    /**
     * 检查结论
     */
    private String resultConclusion;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;
}
