package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单病种质控病例（M4）。病案首页取数快照；{@code (disease_id, admission_id)} 唯一
 * —— 一份病历对同一病种只纳入一次。纳入不可删（留质控底账），错纳入用质控异常标记。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_single_disease_case")
public class BizSingleDiseaseCase extends BaseEntity {

    /**
     * 病例编号
     */
    private String caseNo;

    /**
     * 病种ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long diseaseId;

    /**
     * 住院ID（入院记录的入院ID）
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
     * 主要诊断编码（首页快照）
     */
    private String mainDiagnosisCode;

    /**
     * 主要诊断名称（首页快照）
     */
    private String mainDiagnosisName;

    /**
     * 住院天数（首页快照）
     */
    private Integer inpatientDays;

    /**
     * 住院总费用（首页快照）
     */
    private BigDecimal totalAmount;

    /**
     * 是否手术：0-否 1-是（首页快照）
     */
    private Integer isSurgery;

    /**
     * 死亡标志（首页快照）
     */
    private Integer deathFlag;

    /**
     * 疗效判定（1-治愈 2-好转 3-未愈 4-死亡 5-其他）
     */
    private Integer curativeEffect;

    /**
     * 纳入方式（1-自动扫描 2-手工纳入）
     */
    private Integer enrollWay;

    /**
     * 质控状态（0-待质控 1-通过 2-异常）
     */
    private Integer qcStatus;

    /**
     * 质控异常项（分号分隔）
     */
    private String qcIssues;

    /**
     * 上报状态（0-未上报 1-已上报）
     */
    private Integer reportStatus;

    /**
     * 上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;
}
