package com.his.report.service;

import com.his.report.vo.StatsOverviewVO;

/**
 * 报表统计服务
 */
public interface StatsService {

    StatsOverviewVO overview(String startDate, String endDate);
}
