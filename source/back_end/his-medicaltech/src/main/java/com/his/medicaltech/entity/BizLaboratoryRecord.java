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
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检验记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_laboratory_record")
public class BizLaboratoryRecord extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 检验记录号（唯一）
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
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 检验项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryItemId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 检验科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryDeptId;

    /**
     * 检验科室名称
     */
    private String laboratoryDeptName;

    /**
     * 标本类型（血液、尿液、粪便等）
     */
    private String specimenType;

    /**
     * 标本编号
     */
    private String specimenNo;

    /**
     * 标本状态（1-待采集 2-已采集 3-已接收 4-检测中 5-已完成 6-已退回）
     */
    private Integer specimenStatus;

    /**
     * 采样时间
     */
    private LocalDateTime sampleTime;

    /**
     * 采样人
     */
    private String sampleBy;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 接收人
     */
    private String receiveBy;

    /**
     * 检测时间
     */
    private LocalDateTime executeTime;

    /**
     * 检测人
     */
    private String executeBy;

    /**
     * 检验费用
     */
    private BigDecimal price;

    /**
     * 检验结论/诊断
     */
    private String diagnosis;

    /**
     * 建议
     */
    private String suggestions;

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
     * 记录状态（1-已登记 2-已采样 3-已接收 4-检测中 5-已出结果 6-已审核 7-已发布 8-已取消）
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
     * 取消时间（批次E：退费取消未开始的检验执行记录时写入）
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;
}
