package com.his.clinical.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 待同步的归档记录（archive_status 待归档/已归档且尚无编码任务）。
 */
@Data
public class ArchiveSyncCandidateVO implements Serializable {

    /**
     * 病案归档ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 病案号
     */
    private String recordNo;

    /** 患者姓名 */
    private String patientName;

    /** 科室名称 */
    private String deptName;

    /** 诊断 */
    private String diagnosis;
}