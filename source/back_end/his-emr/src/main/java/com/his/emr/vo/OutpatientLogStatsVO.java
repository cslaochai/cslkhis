package com.his.emr.vo;

import lombok.Data;

/**
 * 门诊日志统计条——与列表共用同一段 WHERE（口径必须跟着筛选走）。
 */
@Data
public class OutpatientLogStatsVO {

    /**
     * 本口径内的接诊病历数
     */
    private Long totalCount;

    /**
     * 发热（体温 ≥ 37.3℃）
     */
    private Long feverCount;

    /**
     * 诊断命中法定传染病目录
     */
    private Long reportableCount;

    /**
     * 应报未报（可报且无报告卡）= 漏报风险
     */
    private Long pendingCount;

    /**
     * 可报且已有报告卡
     */
    private Long reportedCount;
}
