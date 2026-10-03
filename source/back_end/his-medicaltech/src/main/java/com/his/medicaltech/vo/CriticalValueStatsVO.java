package com.his.medicaltech.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 危急值统计出参（对应列表页顶部四张卡片）
 */
@Data
public class CriticalValueStatsVO {

    /**
     * 本月危急值总数
     */
    private Long monthTotal;

    /**
     * 待接收
     */
    private Long pending;

    /**
     * 已接收待处置
     */
    private Long received;

    /**
     * 已处置
     */
    private Long handled;

    /**
     * 超时未处置（实时计算，不是存储状态）
     */
    private Long overdue;

    /**
     * 及时处置率（%，已处置且在时限内的占比）。
     * 无数据时返回 null 而不是 0 —— 「没有数据」与「及时率 0」是两回事。
     */
    private BigDecimal timelyRate;
}
