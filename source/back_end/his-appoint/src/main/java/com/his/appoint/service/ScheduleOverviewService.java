package com.his.appoint.service;

import com.his.appoint.vo.ScheduleOverviewVO;

import java.time.LocalDate;

/**
 * 排班周总览（只读驾驶舱）。
 */
public interface ScheduleOverviewService {

    /**
     * 按周聚合排班全域事实：门诊号源、在岗人次、人力缺口、每日总值班。
     *
     * @param beginDate 周内任意一天，归一到所在周的周一；null 取今天所在周
     */
    ScheduleOverviewVO overviewWeek(LocalDate beginDate);
}
