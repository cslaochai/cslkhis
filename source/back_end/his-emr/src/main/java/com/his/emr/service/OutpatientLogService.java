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

    /**
     * 「我的未报」医生自查分页：强制本人 + 只看应报未报，服务端收口（菜单 2950，sql/238）
     */
    PageResult<OutpatientLogListVO> myPendingPage(OutpatientLogQueryDTO query);

    /**
     * 「我的未报」统计条：与 myPendingPage 同一收口口径
     */
    OutpatientLogStatsVO myPendingStats(OutpatientLogQueryDTO query);
}
