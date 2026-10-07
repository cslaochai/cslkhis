package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 可模拟病案首页列表一行（{@code DrgSimMapper#summaryList}）。
 *
 * <p>LEFT JOIN 已有模拟结果：模拟结果与首页是 1:1（uk_summary），所以 join 不会放大行；
 * 带出 {@code drgCode}/{@code profitAmount} 是为了让页面直接显示"这条模拟过了没有、
 * 盈亏多少"，不必为每行再查一次。
 */
@Data
public class DrgSummaryListRowVO implements Serializable {

    /**
     * 病案首页ID
     */
    private Long summaryId;

    /**
     * 患者姓名（首页快照）
     */
    private String patientName;

    /**
     * 科室名称（首页快照）
     */
    private String deptName;

    /**
     * 出院时间
     */
    private LocalDateTime dischargeTime;

    /**
     * 主要诊断 ICD 编码
     */
    private String mainDiagnosisCode;

    /**
     * 主要诊断名称
     */
    private String mainDiagnosisName;

    /**
     * 是否手术（0-否 1-是）
     */
    private Integer isSurgery;

    /**
     * 已有的模拟结果入组编码（尚未模拟时为 null）
     */
    private String drgCode;

    /**
     * 已有的模拟结果盈亏（尚未模拟时为 null）
     */
    private BigDecimal profitAmount;
}