package com.his.clinical.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 随访看板原始计数（各状态条数 + 今日应随访/逾期/今日已完成/已生成复诊号/总数）。
 */
@Data
public class FollowupStatCountVO implements Serializable {

    /** 待随访条数（随访任务状态 1-待随访） */
    private Long pending;

    /** 随访中条数（随访任务状态 2-随访中） */
    private Long doing;

    /** 已完成条数（随访任务状态 3-已完成） */
    private Long done;

    /** 已取消条数（随访任务状态 4-已取消） */
    private Long cancelled;

    /** 今日应随访条数（计划随访日期为当天且未完成） */
    private Long todayDue;

    /** 逾期未随访条数（计划随访时间已过且未完成，按当前时刻现算不落状态） */
    private Long overdue;

    /** 今日已完成条数（执行日期为当天的完成任务） */
    private Long doneToday;

    /** 已生成复诊预约的条数 */
    private Long revisitCnt;

    /** 全部任务条数 */
    private Long total;
}