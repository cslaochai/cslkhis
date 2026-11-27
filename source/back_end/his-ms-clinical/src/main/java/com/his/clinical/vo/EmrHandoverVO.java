package com.his.clinical.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 病历草稿移交代补 VO
 */
@Data
public class EmrHandoverVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 单号 HD+yyyyMMdd+4位
     */
    private String handoverNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 就诊挂号单ID（取自病历，续写跳转用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 病历号（快照）
     */
    private String recordNo;

    /**
     * 患者ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 就诊日期（取自病历）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 病历所属科室ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 病历所属科室名称（快照）
     */
    private String deptName;

    /**
     * 原经治医生ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long originalDoctorId;

    /**
     * 原经治医生姓名（快照）
     */
    private String originalDoctorName;

    /**
     * 承接人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverId;

    /**
     * 承接人姓名（快照）
     */
    private String receiverName;

    /**
     * 移交原因（1-离职 2-长期休假 3-调离临床岗位 4-其他）
     */
    private Integer reason;

    /**
     * 移交原因文案
     */
    private String reasonText;

    /**
     * 状态（1-待代补 2-已完成 3-已撤销）
     */
    private Integer status;

    /**
     * 状态文案
     */
    private String statusText;

    /**
     * 移交发起人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 移交发起人姓名（快照）
     */
    private String operatorName;

    /**
     * 代补完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 代补完成人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long completeById;

    /**
     * 代补完成人姓名（快照）
     */
    private String completeByName;

    /**
     * 备注/移交说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
