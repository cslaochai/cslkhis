package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 待点评的 I 类切口手术（含围手术期抗菌药物医嘱候选） */
@Data
public class IncisionCandidateVO {

    /** 手术申请单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operationApplyId;

    /** 手术申请单号 */
    private String applyNo;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 手术科室 */
    private String deptName;

    /** 手术名称 */
    private String operationName;

    /** 手术编码 ICD-9-CM-3 */
    private String operationCode;

    /** 手术开始时间（切皮），判定给药时机的锚点 */
    private LocalDateTime operationTime;

    /** 主刀医师 */
    private String surgeonName;

    /** 围手术期抗菌药物医嘱候选（手术前后 24h 内） */
    private List<IncisionDrugCandidateVO> drugCandidates;
}
