package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.OutpatientLogQueryDTO;
import com.his.emr.vo.OutpatientLogListVO;
import com.his.emr.vo.OutpatientLogStatsVO;

/**
 * 门诊日志（法规台账）。只读：报卡写动作一律走 InfectiousReportService，
 */
public interface OutpatientLogService {

    PageResult<OutpatientLogListVO> listPage(OutpatientLogQueryDTO query);

    OutpatientLogStatsVO stats(OutpatientLogQueryDTO query);
}
