package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.OutpatientLogQueryDTO;
import com.his.emr.vo.OutpatientLogListVO;
import com.his.emr.vo.OutpatientLogStatsVO;

/**
 * 门诊日志（法规台账）。只读：报卡写动作一律走 InfectiousReportService，
 */
public interface OutpatientLogService {

    /**
     * 门诊日志分页：按就诊时间、科室、医生与报状态筛就诊行，并标出诊断命中法定传染病目录的应报行。
     * 用于「门诊日志」台账页查询卡与表格；出参手机号只给脱敏列。
     */
    PageResult<OutpatientLogListVO> listPage(OutpatientLogQueryDTO query);

    /**
     * 门诊日志统计条：就诊总数、发热就诊数、应报数、已报数与未报数，与分页同一套筛选口径。
     * 用于「门诊日志」台账页表格上方的统计区。
     */
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
