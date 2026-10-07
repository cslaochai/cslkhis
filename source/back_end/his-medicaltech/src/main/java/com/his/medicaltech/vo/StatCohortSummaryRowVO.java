package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出院队列汇总（{@code StatReportAggMapper#cohortSummary}）。
 *
 * <p>死亡例数的口径只在SQL 里说清：唯一来源是出院记录的 {@code death_flag = 1}
 * （离院方式=死亡与之由出院服务成对校验）。原先取 {@code admit_status = 5} 是错的 ——
 * 全仓没有任何代码写过 admit_status=5，出院只置 0，所以那个口径下死亡例数恒等于 0。
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