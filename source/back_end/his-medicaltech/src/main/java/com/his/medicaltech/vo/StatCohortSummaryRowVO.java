package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出院队列汇总（StatReportAggMapper#cohortSummary）。
 */
@Data
public class StatCohortSummaryRowVO implements Serializable {

    /**
     * 出院例数
     */
    private Long dischargeCount;

    /**
     * 死亡例数
     */
    private Long deathCount;

    /**
     * 平均住院日（天）。脏数据 discharge &lt; admit 按行钳 0，报表不出负数
     */
    private BigDecimal avgLosDays;
}