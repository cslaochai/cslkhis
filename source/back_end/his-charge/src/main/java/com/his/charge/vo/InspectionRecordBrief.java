package com.his.charge.vo;

import com.his.charge.service.MedicalTechGateway;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查记录跨域摘要。
 *
 * @see MedicalTechGateway
 */
@Data
@NoArgsConstructor
public class InspectionRecordBrief {

    /** 检查记录ID */
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 检查项目名 */
    private String inspectionItemName;

    /** 临床诊断（检查所见/印象） */
    private String clinicalDiagnosis;

    /** 检查结论 */
    private String resultConclusion;

    /** 就诊日期 */
    private LocalDate visitDate;
}
