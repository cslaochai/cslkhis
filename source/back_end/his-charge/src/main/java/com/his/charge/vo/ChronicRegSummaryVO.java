package com.his.charge.vo;

import lombok.Data;

/**
 * 慢特病备案汇总（后端 SQL 聚合）。
 */
@Data
public class ChronicRegSummaryVO {

    /**
     * 当前有效（含已过期未注销的单据数按展示态拆开）
     */
    private Integer validCount;

    /**
     * 有效但已过待遇终止日（待续备名单）
     */
    private Integer expiredCount;

    /**
     * 已注销单数
     */
    private Integer cancelledCount;

    /**
     * 已驳回单数
     */
    private Integer rejectedCount;
}
