package com.his.patient.vo;

import lombok.Data;

/**
 * 等床队列概览（顶部统计条）
 */
@Data
public class BedWaitStatsVO {

    /**
     * 等待中：普通 / 急 / 危重
     */
    private long waitingNormal;
    private long waitingUrgent;
    private long waitingCritical;
    /**
     * 等待中合计
     */
    private long waitingTotal;

    /**
     * 已安排床位（预留中，等待入院）
     */
    private long arrangedCount;

    /**
     * 今日收治（本_wait 队列里完成的）
     */
    private long admittedToday;

    /**
     * 今日取消
     */
    private long cancelledToday;

    /**
     * 等待超时（超过最长等待天数仍在等待中）
     */
    private long overdueCount;

    /**
     * 平均等待时长（小时，只统计等待中的）
     */
    private long avgWaitHours;

    /**
     * 最长等待时长（小时）
     */
    private long maxWaitHours;

    /**
     * 最长等待天数的阈值（来自配置，写出来是为了不让前端猜）
     */
    private int maxWaitDays;

    // 床位侧

    private long totalBeds;
    private long freeBeds;
    /**
     * 已被预留（锁定）的床位
     */
    private long lockedBeds;
    private long occupiedBeds;

    /**
     * 院内跨科调配进行中的数量
     */
    private long crossDeptCount;
}
