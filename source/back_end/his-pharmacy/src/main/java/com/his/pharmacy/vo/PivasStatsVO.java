package com.his.pharmacy.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 静配中心统计 VO（按明细状态计数 + 今日主单数）。
 */
@Data
public class PivasStatsVO implements Serializable {

    /**
     * 待审方明细数
     */
    private Long pendingAudit;

    /**
     * 已审方（待排队）明细数
     */
    private Long audited;

    /**
     * 已排队（待调配）明细数
     */
    private Long queued;

    /**
     * 已调配（待核对）明细数
     */
    private Long compounded;

    /**
     * 已核对发放明细数
     */
    private Long verified;

    /**
     * 已拒配明细数
     */
    private Long rejected;

    /**
     * 今日静配主单张数
     */
    private Long batchCount;
}
