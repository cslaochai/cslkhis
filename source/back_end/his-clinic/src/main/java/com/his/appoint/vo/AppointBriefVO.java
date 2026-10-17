package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 挂号跨域摘要：病历域只读挂号记录的最小字段集。
 */
@Data
@NoArgsConstructor
public class AppointBriefVO {

    /**
     * 挂号ID（挂号记录主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 挂号单号
     */
    private String registNo;

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
     * 接诊医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 接诊医生姓名
     */
    private String doctorName;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;
}
