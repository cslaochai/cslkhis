package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CDR 挂号行（CdrMapper#selectRegistrations 一行）。
 */
@Data
public class CdrRegistrationRowVO implements Serializable {

    /**
     * 挂号ID（字符串，避免前端丢精度）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 挂号单号
     */
    private String registNo;

    /**
     * 数据归属档案ID（EMPI 归并后可能不是主档）
     */
    private String ownerPid;

    /**
     * 挂号时间
     */
    private LocalDateTime registTime;

    /**
     * 就诊日期（挂号时间为空时用它兜底节点起始时间）
     */
    private LocalDate visitDate;

    /**
     * 挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号 7-爽约 8-未就诊）
     */
    private Integer registStatus;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）
     */
    private Integer registType;

    /**
     * 医保类型（如：在职职工、退休职工、城乡居民等；存名称非编码）
     */
    private String medicalInsuranceType;
}