package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 可入径候选/入院快照 VO（入院记录 × 患者基本信息）。
 */
@Data
public class PathwayAdmissionVO implements Serializable {

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    private String admissionNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 入院科室ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 入院科室名称（快照） */
    private String deptName;

    /** 入院诊断（文本快照） */
    private String diagnosis;

    /** 入院时间（DATE_FORMAT 出串，避免跨层日期漂移） */
    private String admitTime;

    /** 在院状态（仅快照查询回填；候选查询恒为在院） */
    private Integer admitStatus;
}
