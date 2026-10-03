package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.OutpatientLogQueryDTO;
import com.his.emr.vo.OutpatientLogListVO;
import com.his.emr.vo.OutpatientLogStatsVO;
import com.his.emr.service.InfectiousReportService;

/**
 * 门诊日志（法规台账）。只读：报卡写动作一律走 {@code InfectiousReportService}，
 * 本服务不建第二套上报状态。
 */
public interface OutpatientLogService {

    PageResult<OutpatientLogListVO> listPage(OutpatientLogQueryDTO query);

    OutpatientLogStatsVO stats(OutpatientLogQueryDTO query);
}
