package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 住院责任护士关系出参。
 */
@Data
public class ChargeNurseRelationVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private String patientName;

    /** 责任护士ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /** 责任护士姓名 */
    private String nurseName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /** 关系类型（1-责任护士） */
    private Integer relationType;
    private String relationTypeText;

    /** 状态（1-有效 0-已结束） */
    private Integer status;
    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    private String remark;
}
