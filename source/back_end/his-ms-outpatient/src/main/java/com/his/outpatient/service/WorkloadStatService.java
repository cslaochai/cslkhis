package com.his.outpatient.service;

import com.his.outpatient.vo.WorkloadStatVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 全岗位工时统计服务。
 */
public interface WorkloadStatService {

    /**
     * 按月统计全岗位工时。
     * @param monthStart 月份第一天（如 2026-10-01）
     * @return 按人员聚合的工时统计列表
     */
    List<WorkloadStatVO> monthlyStats(LocalDate monthStart);
}
