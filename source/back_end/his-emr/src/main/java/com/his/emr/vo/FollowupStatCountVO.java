package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 随访看板原始计数（各状态条数 + 今日应随访/逾期/今日已完成/已生成复诊号/总数）。
 *
 * <p>完成率不在这里算：分母是「待随访 + 随访中 + 已完成」三档之和，
 * 这个口径属于业务判断，放在 service 收口。
 */
@Data
public class FollowupStatCountVO implements Serializable {

    private Long pending;

    private Long doing;

    private Long done;

    private Long cancelled;

    private Long todayDue;

    private Long overdue;

    private Long doneToday;

    private Long revisitCnt;

    private Long total;
}