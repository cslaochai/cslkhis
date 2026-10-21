package com.his.medicaltech.service;

import com.his.medicaltech.vo.StatsOverviewVO;

/**
 * 报表统计服务
 */
public interface StatsService {

    /**
     * 按日期区间聚合门诊、住院、收入与药事的总量指标。
     * 用于运营统计报表页选好时间范围后渲染顶部总览卡片。
     */
    StatsOverviewVO overview(String startDate, String endDate);
}
