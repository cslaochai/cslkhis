package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CDR 住院节点行（CdrMapper#selectAdmissions 一行）。
 */
@Data
public class CdrAdmissionRowVO implements Serializable {

    /**
     * 入院记录ID（字符串，避免前端丢精度）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 数据归属档案ID（EMPI 归并后可能不是主档）
     */
    private String ownerPid;

    /**
     * 入院时间
     */
    private LocalDateTime admitTime;

    /**
     * 出院时间（在院时为 null）
     */
    private LocalDateTime dischargeTime;

    /**
     * 入院状态（0-已出院 1-在院）
     */
    private Integer admitStatus;

    /**
     * 入院途径（1-门诊 2-急诊 3-转院 4-其他）
     */
    private Integer admitWay;

    /**
     * 入院诊断（优先病案首页主诊断，回落入院记录诊断）
     */
    private String diagnosis;

    /**
     * 科室名称（子查询补，科室被删时为 null）
     */
    private String deptName;

    /**
     * 病区名称（子查询补）
     */
    private String wardName;

    /**
     * 床号（子查询补）
     */
    private String bedNo;

    /**
     * 病案首页主诊断（作为节点 outcome 的权威来源，优先于 admission 快照）
     */
    private String summaryDiag;

    /**
     * 病案首页状态（1-草稿 2-已提交 3-已归档）
     */
    private Integer summaryStatus;
}