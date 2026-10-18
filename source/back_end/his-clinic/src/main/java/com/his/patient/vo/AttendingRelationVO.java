package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 住院管床关系出参。
 */
@Data
public class AttendingRelationVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 管床医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 医生姓名
     */
    private String employeeName;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 管床关系类型（1-主管 2-主诊组长 3-协作）
     */
    private Integer relationType;
    /**
     * 管床关系类型文案
     */
    private String relationTypeText;

    /**
     * 状态（1-有效 0-已结束）
     */
    private Integer status;
    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 关系生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    /**
     * 关系结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    private String remark;
}
