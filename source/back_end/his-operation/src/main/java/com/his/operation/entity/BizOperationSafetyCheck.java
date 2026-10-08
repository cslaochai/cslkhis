package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 手术安全核查单（手术安全核查单）—— 三方 × 三时段，一时段一行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_operation_safety_check")
public class BizOperationSafetyCheck extends BaseEntity {

    /**
     * 核查单号（HC + yyyyMMdd + 4位序号）
     */
    private String checkNo;

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 手术申请单号
     */
    private String applyNo;

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
     * 手术名称（快照，拟施）
     */
    private String operationName;

    /**
     * 手术间
     */
    private String operationRoom;

    /**
     * 核查时段：1-麻醉诱导前(Sign In) 2-手术开始前(Time Out) 3-患者离开手术室前(Sign Out)
     */
    private Integer phase;

    /**
     * 核查项码值（逗号分隔）
     */
    private String items;

    /**
     * 异常说明
     */
    private String note;

    /**
     * 手术医师员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long surgeonId;

    /**
     * 手术医师姓名
     */
    private String surgeonName;

    /**
     * 手术医师签名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime surgeonSignTime;

    /**
     * 麻醉医师员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anesthetistId;

    /**
     * 麻醉医师姓名
     */
    private String anesthetistName;

    /**
     * 麻醉医师签名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime anesthetistSignTime;

    /**
     * 手术室护士（器械/巡回）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 手术室护士姓名
     */
    private String nurseName;

    /**
     * 手术室护士签名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nurseSignTime;

    /**
     * 录入人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recorderId;

    /**
     * 录入人姓名
     */
    private String recorderName;

    /**
     * 核查完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;
}
