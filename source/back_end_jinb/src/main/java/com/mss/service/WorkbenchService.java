package com.mss.service;

import com.mss.vo.StatsVO;
import com.mss.vo.WorkbenchVO;

/**
 * 经办工作台 + 统计报表
 */
public interface WorkbenchService {

    WorkbenchVO summary();

    StatsVO stats();
}
