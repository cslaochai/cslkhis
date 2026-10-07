package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台折线卡的一日一点（{@code WorkbenchMetricMapper#weekRegistTrend} 一行）。
 *
 * <p>字段名是前端契约：{@code WeekTrendWidget.vue} 按 {@code item.date}（截 MM-DD 作轴标签）
 * 与 {@code item.cnt}（柱高与顶部数字）取值。
 *
 * <p>SQL 只对"有数据的日期"分组，周末没人挂号就少一天 —— 补零在 Provider 侧做，
 * 因为只有那边知道要展示哪7 天（哪天到今天）。
 */
@Data
public class WorkbenchWeekTrendRowVO implements Serializable {

    /**
     * 就诊日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 当日挂号数
     */
    private Long cnt;
}