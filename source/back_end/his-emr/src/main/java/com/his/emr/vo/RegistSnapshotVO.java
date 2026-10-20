package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 门诊挂号单快照（治疗开单时取患者/科室/医生，作为收费明细的来源依据）。
 */
@Data
public class RegistSnapshotVO implements Serializable {

    /** 挂号ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /** 挂号单号 */
    private String registNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 就诊科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 就诊科室名称 */
    private String deptName;

    /** 接诊医生ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 接诊医生姓名 */
    private String doctorName;

    /**
     * 挂号状态（1-正常 5-已取消等）
     */
    private Integer registStatus;

    /**
     * 退号时间（非空即已退号）
     */
    private LocalDateTime refundTime;
}