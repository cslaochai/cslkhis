package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 慢病建档行出参（患者端「我的慢病档案」与建档列表共用）。
 */
@Data
public class ChronicRecordListVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 档案编号 */
    private String recordNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 慢病编码（ICD-10） */
    private String diseaseCode;

    /** 慢病名称 */
    private String diseaseName;

    /** 认定医生ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 认定医生姓名 */
    private String doctorName;

    /** 认定科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 认定科室名称 */
    private String deptName;

    /** 认定状态（0-待认定 1-已认定 2-已取消） */
    private Integer confirmStatus;

    /** 认定时间 */
    private LocalDateTime confirmTime;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
