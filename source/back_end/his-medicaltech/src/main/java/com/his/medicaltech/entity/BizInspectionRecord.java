package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inspection_record")
public class BizInspectionRecord extends BaseEntity {
    /**
     * 检查记录号（唯一）
     */
    private String recordNo;

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
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 申请科室ID
     */
    private Long applyDeptId;

    /**
     * 申请科室
     */
    private String applyDeptName;

    /**
     * 申请医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生
     */
    private String applyDoctorName;

    /**
     * 检查项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionItemId;

    /**
     * 检查项目编码
     */
    private String inspectionItemCode;

    /**
     * 检查项目名称
     */
    private String inspectionItemName;

    /**
     * 检查科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionDeptId;

    /**
     * 检查科室名称
     */
    private String inspectionDeptName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String inspectionPurpose;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 检查费用
     */
    private BigDecimal price;

    /**
     * 预约时间
     */
    private LocalDateTime appointmentTime;

    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 执行人
     */
    private String executeBy;

    /**
     * 检查描述
     */
    private String resultDescription;

    /**
     * 检查结论
     */
    private String resultConclusion;

    /**
     * 报告医师签名ID（电子签名证据的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    /**
     * 报告签名时刻
     */
    private LocalDateTime reportSignedTime;

    /**
     * 检查影像路径
     */
    private String resultImage;

    /**
     * 记录状态（1-已登记 2-已签到 3-检查中 4-已出结果 5-已审核 6-已发布 7-已取消）
     */
    private Integer recordStatus;

    /**
     * 审核人（初审）
     */
    private String auditBy;

    /**
     * 审核医师签名ID（电子签名证据的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /**
     * 审核签名时刻
     */
    private LocalDateTime auditSignedTime;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 复审人
     */
    private String audit2By;

    /**
     * 复审时间
     */
    private LocalDateTime audit2Time;

    /**
     * 报告发布时间
     */
    private LocalDateTime reportTime;

    /**
     * 报告发布人
     */
    private String reportBy;

    /**
     * 取消时间（批次E：退费取消未开始的检查执行记录时写入）
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;
}
