package com.his.system.api.vo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 病历简要信息
 */
@Data
@NoArgsConstructor
public class RecordBriefVO {
    private Long id;
    private String recordNo;
    private Long patientId;
    private String patientName;
    private LocalDate visitDate;
    /**
     * 就诊类型（1-初诊 2-复诊），见 VisitTypeEnum
     */
    private Integer visitType;
    /**
     * 原就诊科室ID/医生ID：复诊收费策略要按「是否同科、是否同医生」判定，
     * 只有名字没法比（同名医生、科室改名都会判错）。
     */
    private Long deptId;
    private Long doctorId;
    private String deptName;
    private String doctorName;
    private String diagnosisName;
}