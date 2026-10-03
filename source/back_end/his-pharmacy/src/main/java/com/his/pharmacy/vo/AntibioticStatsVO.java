package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 抗菌药物监测指标行（月度快照，分子分母一并带出供复核） */
@Data
public class AntibioticStatsVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 统计月份 yyyy-MM */
    private String statMonth;

    /** 统计范围（1-全院 2-科室） */
    private Integer scopeType;

    private String scopeTypeText;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称（快照） */
    private String deptName;

    /** 门急诊处方总数（处方状态 3/4，源 1/2） */
    private Integer opRxCount;

    /** 含抗菌药物的门急诊处方数 */
    private Integer opAbxRxCount;

    /** 门诊抗菌药物使用率（%） */
    private BigDecimal opUsageRate;

    /** 同期出院患者数 */
    private Integer ipDischargeCount;

    /** 出院患者中使用抗菌药物的人数 */
    private Integer ipAbxPatientCount;

    /** 住院抗菌药物使用率（%） */
    private BigDecimal ipUsageRate;

    /** 收治患者人天数 */
    private Integer patientDays;

    /** 抗菌药物累计 DDD 数 */
    private BigDecimal ddds;

    /** 使用强度 AUD */
    private BigDecimal aud;

    /** 使用抗菌药物的住院患者数 */
    private Integer abxTreatCount;

    /** 其中送检微生物标本的患者数 */
    private Integer microSubmitCount;

    /** 微生物标本送检率（%） */
    private BigDecimal microSubmitRate;

    /** 未匹配到抗菌药物目录的住院药品医嘱数 */
    private Integer unmatchedOrderCount;

    /** 目标值对照（评审/专项整治常用阈值；只作提示，不是判定） */
    private BigDecimal audTarget;

    private BigDecimal opUsageRateTarget;

    private BigDecimal ipUsageRateTarget;

    private BigDecimal microSubmitRateTarget;

    /** 生成人 */
    private String generateBy;

    /** 生成时间 */
    private LocalDateTime generateTime;

    /** 备注 */
    private String remark;
}
