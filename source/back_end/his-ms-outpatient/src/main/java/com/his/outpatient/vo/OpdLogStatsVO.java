package com.his.outpatient.vo;

import lombok.Data;

/**
 * 门诊日志统计条（跟随筛选条件，逐项与列表同一套 WHERE）。
 */
@Data
public class OpdLogStatsVO {

    /**
     * 总就诊（该筛选下的挂号数，含还没入队的）
     */
    private Long total;

    /**
     * 未缴费（挂号已登记、未生成收费单、未入队）
     */
    private Long unpaid;

    /**
     * 待签到（已开收费单或已签到，但还没有队列行）
     */
    private Long waitCheckIn;

    /**
     * 候诊中（队列已签到入队、尚未叫号）
     */
    private Long waiting;

    /**
     * 就诊中（医生已开始接诊、还没结诊）
     */
    private Long consulting;

    /**
     * 已就诊（本次接诊已完成）
     */
    private Long completed;

    /**
     * 已退号：含库里 164 条「退号未收费、没有队列行」的挂号（只看队列行会漏掉绝大多数退号）
     */
    private Long cancelled;

    /**
     * 已过号：叫了号患者没来（与 unvisited 区分，本项是「被叫到过」）
     */
    private Long overdue;

    /**
     * 未就诊：到院签到过、当天没被接诊 —— 日终结转落的终态（挂号 8 / 队列 7）。
     * 与 overdue 的区别：overdue 是「叫了号没来」，本项是「压根没被叫到」。
     */
    private Long unvisited;

    /**
     * 爽约：挂了号但当天没到院签到（挂号 7）
     */
    private Long noShow;

    /**
     * 就诊状态推导不出来的行数（队列状态为历史脏数据 1）。
     * 单独列出来是为了「不静默丢数据」——这些行不计入上面任何一项。
     */
    private Long unrecognized;

    /**
     * 平均候诊时长（分钟）= AVG(开始就诊 - 到达)，只统计落在 [0, 12 小时] 的样本。
     */
    private Long avgWaitMinutes;

    /**
     * 平均就诊时长（分钟）= AVG(结束 - 开始)，只统计落在 (0, 12 小时] 的样本 ——
     * 库里存在跨天没关诊留下的 959 分钟这种值，计入平均值会把整体拉失真。
     */
    private Long avgVisitMinutes;
}
