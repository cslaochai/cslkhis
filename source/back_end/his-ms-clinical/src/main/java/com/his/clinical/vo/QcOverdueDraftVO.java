package com.his.clinical.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 质控侧超时草稿看板行 VO
 */
@Data
public class QcOverdueDraftVO {

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 挂号单ID（续写跳转用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 病历号
     */
    private String recordNo;

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
     * 性别
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

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
     * 经治医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 经治医生姓名
     */
    private String doctorName;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 已超天数（就诊日期距今天数）
     */
    private Long overdueDays;

    /**
     * 移交单ID（待代补时非空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long handoverId;

    /**
     * 移交单号
     */
    private String handoverNo;

    /**
     * 移交状态（空-未移交；1-待代补）
     */
    private Integer handoverStatus;

    /**
     * 移交状态文案
     */
    private String handoverStatusText;

    /**
     * 承接人姓名（待代补时）
     */
    private String receiverName;

    /**
     * 移交原因码（service 侧转文案）
     */
    private Integer reason;

    /**
     * 移交原因文案
     */
    private String reasonText;
}
