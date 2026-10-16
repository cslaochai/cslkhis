package com.his.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临床闭环（手术、输血等）回写正式文书的入参。
 * 正文由发起方组装，提供方只负责病历号取号与落库。
 */
@Data
public class ClosedLoopRecordDTO {

    private Long admissionId;

    private Long patientId;

    private String patientNo;

    private String patientName;

    private Integer gender;

    private Integer age;

    private Integer ageUnit;

    private Long deptId;

    private String deptName;

    private Long wardId;

    private String wardName;

    private String bedNo;

    /**
     * 文书类型（com.his.common.enums.InpatientRecordTypeEnum）
     */
    private Integer recordType;

    private String recordTitle;

    private LocalDateTime recordTime;

    private String diagnosisName;

    private String courseNote;

    private String treatmentPlan;

    private String remark;

    /**
     * 文书状态（com.his.common.enums.InpatientRecordStatusEnum）
     */
    private Integer recordStatus;

    private Long doctorId;

    private String doctorName;

    private LocalDateTime submitTime;
}
