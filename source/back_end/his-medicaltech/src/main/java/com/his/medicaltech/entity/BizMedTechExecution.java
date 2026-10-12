package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医技执行记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_medicaltech_execution")
public class BizMedTechExecution extends BaseEntity {

    /**
     * 执行单号
     */
    private String executionNo;
    /**
     * 申请类型（1-检查 2-检验）
     */
    private Integer applyType;

    /**
     * 申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;
    /**
     * 申请单号
     */
    private String applyNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 患者号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;
    /**
     * 项目编码
     */
    private String itemCode;
    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 执行状态（1-待执行 2-执行中 3-已完成 4-已审核）
     */
    private Integer executionStatus;

    /**
     * 执行人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executorId;
    /**
     * 执行人姓名
     */
    private String executorName;
    /**
     * 执行时间
     */
    private LocalDateTime executeTime;
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewerId;
    /**
     * 审核人姓名
     */
    private String reviewerName;
    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
