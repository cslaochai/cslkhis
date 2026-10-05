package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公卫上报记录出参
 */
@Data
public class BizPublicHealthReportVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 上报单号
     */
    private String reportNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
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
     * 上报状态：1-待审核 2-审核通过 3-审核驳回
     *
     * <p>口径以列注释公卫上报表的报告状态为准，与字典 his_ph_report_status 一致。
     * 旧注释「待上报/已上报/已审核」是错的，已纠正。</p>
     */
    private Integer reportStatus;

    /**
     * 上报人
     */
    private String reportBy;

    /**
     * 上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /**
     * 审核人
     */
    private String auditBy;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

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
     * 删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
