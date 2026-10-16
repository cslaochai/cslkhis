package com.his.pharmacy.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 住院摆药统计 VO（按明细状态计数）。
 */
@Data
public class WardDispenseStatsVO implements Serializable {

    /**
     * 待配药明细数
     */
    private Long pending;

    /**
     * 已配药（待核对）明细数
     */
    private Long dispensed;

    /**
     * 已核对明细数
     */
    private Long checked;

    /**
     * 已退药明细数
     */
    private Long returned;

    /**
     * 今日摆药单张数
     */
    private Long dispenseCount;
}
