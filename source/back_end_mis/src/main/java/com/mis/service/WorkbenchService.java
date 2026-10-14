package com.mis.service;

import com.mis.vo.StatsVO;
import com.mis.vo.WorkbenchVO;

/**
 * 经办工作台 + 统计报表
 */
public interface WorkbenchService {

    WorkbenchVO summary();

    StatsVO stats();
}
