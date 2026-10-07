package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工作台数字卡「全院今日概况」（{@code WorkbenchMetricMapper#hospitalCoreStats}）。
 *
 * <p>字段名是<b>前端契约</b>：{@code front/src/lib/workbench-widgets.js} 的
 * {@code METRIC_SPECS.hospitalToday} 按 {@code todayRegistCount / todayRevenue /
 * inHospitalCount / bedOccupied + pairKey=bedTotal} 这些 key 取值渲染，
 * 改名 = 那张卡的数字全变「—」，且不会报错。
 *
 * <p>今日实收净额 = L3 今日流水净额（收正退负一起 SUM）：按收费单状态反推会把
 * "昨天收、今天退"的那笔漏掉，收银员点钞数就对不上。
 */
@Data
public class WorkbenchHospitalCoreRowVO implements Serializable {

    /**
     * 今日挂号数
     */
    private Long todayRegistCount;

    /**
     * 今日实收净额
     */
    private BigDecimal todayRevenue;

    /**
     * 在院患者数
     */
    private Long inHospitalCount;

    /**
     * 床位总数（占用率的分母用它，不是"已占床数"）
     */
    private Long bedTotal;

    /**
     * 占用床位数
     */
    private Long bedOccupied;
}