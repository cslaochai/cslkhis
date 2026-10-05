package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病案编码任务 VO
 */
@Data
public class ArchiveCodeTaskVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 任务号 CT+yyyyMMdd+4位
     */
    private String taskNo;

    /**
     * 归档记录病历归档的ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveId;

    /**
     * 病历号（快照）
     */
    private String recordNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 病历所属科室（快照）
     */
    private String deptName;

    /**
     * 病历诊断（快照，编码前的人工参照）
     */
    private String diagnosis;

    /**
     * 状态（1-待编码 2-已提交 3-已完成 4-已退修）
     */
    private Integer status;

    /**
     * 编码人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long coderId;

    /**
     * 编码员姓名（快照）
     */
    private String coderName;

    /**
     * 分配时间
     */
    private LocalDateTime assignTime;

    /**
     * 主诊断 ICD-10 编码
     */
    private String mainIcdCode;

    /**
     * 主诊断名称
     */
    private String mainIcdName;

    /**
     * 其他诊断/手术 ICD（文本，分号分隔）
     */
    private String otherIcdText;

    /**
     * 提交编码时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditById;

    /**
     * 审核人姓名（快照）
     */
    private String auditByName;

    /**
     * 审核意见（退修必填）
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 累计退修次数
     */
    private Integer returnCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
