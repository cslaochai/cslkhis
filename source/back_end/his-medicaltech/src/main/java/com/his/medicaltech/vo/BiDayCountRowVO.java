package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 近 7 日挂号量趋势点（{@code BiMapper#appointmentTrend} 一行）。
 *
 * <p>SQL 只对"有数据的日期"分组，周末没人挂号就少一天 ——
 * 缺日补零在服务层做（{@link BiOverviewVO.TrendPoint}），因为补零要知道日历的起止。
 */
@Data
public class BiDayCountRowVO implements Serializable {

    /**
     * 统计日（MM-dd）
     */
    private String statDate;

    /**
     * 当日挂号数
     */
    private Long cnt;
}