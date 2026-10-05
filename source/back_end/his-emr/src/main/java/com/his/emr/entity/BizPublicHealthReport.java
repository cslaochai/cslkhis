package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公卫上报表
 */
@Data
@TableName("biz_public_health_report")
public class BizPublicHealthReport {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 上报编号
     */
    private String reportNo;

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
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 上报类型（1-传染病 2-死因监测 3-慢性病 4-其他）
     */
    private Integer reportType;
    /**
     * 上报内容
     */
    private String reportContent;
    /**
     * 诊断
     */
    private String diagnosis;
    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /**
     * 上报状态（1-待审核 2-审核通过 3-审核驳回）
     */
    private Integer reportStatus;
    /**
     * 上报人
     */
    private String reportBy;
    /**
     * 上报时间
     */
    private LocalDateTime reportTime;

    /**
     * 审核人
     */
    private String auditBy;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 审核意见
     */
    private String auditRemark;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
