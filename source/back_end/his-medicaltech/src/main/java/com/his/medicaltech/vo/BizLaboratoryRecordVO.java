package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检验记录出参
 */
@Data
public class BizLaboratoryRecordVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 检验记录号（唯一）
     */
    private String recordNo;

    /**
     * 检验申请ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 检验申请单号
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
     * 性别：1-男 2-女
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 开单科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 开单科室名称
     */
    private String applyDeptName;

    /**
     * 开单医生姓名
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
     * 执行检验科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryDeptId;

    /**
     * 执行检验科室名称
     */
    private String laboratoryDeptName;

    /**
     * 标本类型（如静脉血、尿液）
     */
    private String specimenType;

    /**
     * 标本条码号
     */
    private String specimenNo;

    /**
     * 标本状态（1-待采集 2-已采集 3-已接收 4-检测中 5-已完成 6-已退回）
     */
    private Integer specimenStatus;

    /**
     * 标本采集时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sampleTime;

    /**
     * 标本采集人
     */
    private String sampleBy;

    /**
     * 标本接收时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    /**
     * 标本接收人
     */
    private String receiveBy;

    /**
     * 检验执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 检验执行人
     */
    private String executeBy;

    /**
     * 项目价格，单位：元
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
     * 记录状态：1-待采集 2-已采集 3-已接收 4-检验中 5-已出结果 6-已审核 7-已发布
     */
    private Integer recordStatus;

    /**
     * 审核人（初审）
     */
    private String auditBy;

    /**
     * 初审时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 终审人
     */
    private String audit2By;

    /**
     * 终审时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime audit2Time;

    /**
     * 报告生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /**
     * 报告发布人
     */
    private String reportBy;

    /**
     * 报告医师签名ID（为空 = 未签报告名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportSignId;

    /**
     * 报告签名时刻
     */
    private LocalDateTime reportSignedTime;

    /**
     * 审核医师签名ID（为空 = 未签审核名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /**
     * 审核签名时刻
     */
    private LocalDateTime auditSignedTime;
}
