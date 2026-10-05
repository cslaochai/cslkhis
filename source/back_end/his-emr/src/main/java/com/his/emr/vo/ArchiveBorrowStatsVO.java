package com.his.emr.vo;

import lombok.Data;

/**
 * 病案借阅/复印工作台统计 VO
 */
@Data
public class ArchiveBorrowStatsVO {

    /**
     * 待审核
     */
    private Long pending;

    /**
     * 已借出（未还）
     */
    private Long lentOut;

    /**
     * 超期未还（已借出且应还日期早于今日）
     */
    private Long overdue;

    /**
     * 已归还（累计）
     */
    private Long returned;
}
