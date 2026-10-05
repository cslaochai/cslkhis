package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.emr.support.QcIssue;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 质控检查记录出参。
 *
 * <p>除实体字段外，附带 {@code recordNo/patientName/deptName} 这些**跨表快照**，
 * 以及各类码值的中文（{@code qcTypeText} 等）—— 中文一律由后端给，
 * 前端不再自己维护一份码值表：两份码表一定会有一份先过期。
 */
@Data
public class BizQualityControlVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 质控单号
     */
    private String qcNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 质控对象来源（OUTPATIENT / INPATIENT）
     */
    private String recordSource;

    /**
     * 来源中文
     */
    private String recordSourceText;

    /**
     * 病历号（跨表取；病历已删则为空）
     */
    private String recordNo;

    /**
     * 住院文书类型；门诊病历为空
     */
    private Integer recordType;

    /**
     * 文书类型中文
     */
    private String recordTypeText;

    /**
     * 病历状态
     */
    private Integer recordStatus;

    /**
     * 病历状态中文
     */
    private String recordStatusText;

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
     * 患者编号
     */
    private String patientNo;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 书写医生
     */
    private String doctorName;

    /**
     * 质控类型（0-综合 1-完整性检查 2-规范性检查 3-逻辑性检查 4-AI内涵质控）
     */
    private Integer qcType;

    /**
     * 质控类型中文
     */
    private String qcTypeText;

    /**
     * 质控内容
     */
    private String qcContent;

    /**
     * 质控结果（0-不通过 1-通过）
     */
    private Integer qcResult;

    /**
     * 质控结果中文
     */
    private String qcResultText;

    /**
     * 错误数量
     */
    private Integer errorCount;

    /**
     * 错误详情（明细的拼接摘要，按 varchar(1000) 截断）
     */
    private String errorDetail;

    /**
     * 质控得分（100 分制）；NULL = 旧版质控未评分
     */
    private Integer score;

    /**
     * 病历质量等级（甲 / 乙 / 丙）；无得分时为 NULL
     */
    private String gradeText;

    /**
     * 最高问题严重度（0-无问题 1-提示 2-重要 3-否决项）
     */
    private Integer severityMax;

    /**
     * 最高严重度中文
     */
    private String severityMaxText;

    /**
     * 质控状态（1-待处理 2-已处理 3-已忽略）
     */
    private Integer qcStatus;

    /**
     * 质控状态中文
     */
    private String qcStatusText;

    /**
     * 质控人
     */
    private String qcBy;

    /**
     * 质控时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime qcTime;

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
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 问题明细；仅详情接口返回，列表接口为 null
     */
    private List<QcIssue> issues;
}
