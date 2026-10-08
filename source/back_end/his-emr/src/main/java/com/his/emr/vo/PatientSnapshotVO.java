package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 患者基本信息快照（跨模块裸 SQL 读患者档案）。
 */
@Data
public class PatientSnapshotVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private String patientNo;

    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    private String phone;

    /**
     * 最近就诊科室ID（快照口径，非当前在岗科室）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;
}