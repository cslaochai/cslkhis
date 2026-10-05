package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 质控检查记录
 */
@Data
@TableName("biz_quality_control")
public class BizQualityControl {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 质控编号
     */
    private String qcNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 质控对象来源（OUTPATIENT-门诊病历 INPATIENT-住院文书）。
     * <p>补这一列的原因：record_id 原先同时装过门诊病历、住院文书、病案归档表三种 ID，
     * 查询侧无法判断该 JOIN 哪张表。
     */
    private String recordSource;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 质控类型（0-综合 1-完整性检查 2-规范性检查 3-逻辑性检查 4-AI内涵质控）
     */
    private Integer qcType;

    /**
     * 检查内容
     */
    private String qcContent;
    /**
     * 检查结果（0-不通过 1-通过）
     */
    private Integer qcResult;
    /**
     * 错误数量
     */
    private Integer errorCount;
    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 质控得分（100 分制；甲级≥90 乙级75~89 丙级&lt;75）。NULL 表示旧版质控未评分
     */
    private Integer score;

    /**
     * 最高问题严重度（0-无问题 1-提示 2-重要 3-否决项）。0 是「无问题」，不是未知码值
     */
    private Integer severityMax;

    /**
     * 质控状态（1-待处理 2-已处理 3-已忽略）
     */
    private Integer qcStatus;
    /**
     * 质控人
     */
    private String qcBy;
    /**
     * 质控时间
     */
    private LocalDateTime qcTime;

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
