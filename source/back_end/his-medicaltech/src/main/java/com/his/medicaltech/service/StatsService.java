package com.his.medicaltech.service;

import com.his.medicaltech.vo.StatsOverviewVO;

/**
 * 报表统计服务
 */
public interface StatsService {

    StatsOverviewVO overview(String startDate, String endDate);
}
