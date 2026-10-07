package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台数字卡「我（医生）的今日诊疗」（{@code WorkbenchMetricMapper#doctorStats}）。
 *
 * <p>字段名是前端契约（{@code METRIC_SPECS.myClinicalToday}），后四项标了
 * {@code danger: true}（待办积压，值 &gt; 0 标红）。
 *
 * <p>口径全部在 SQL 里按 {@code employeeId} 收敛，不接受前端传参 ——
 * 否则会变成"工作台看全院、点进去只有本科室"。
 * {@code todoConsultationCount} 里的 {@code to_dept_id} 是唯一按科室收敛的一项
 * （会诊单可以发给科室而非个人），所以 deptId 也必须带上。
 *
 * <p>「今日排班」只算出诊班：sql/195 起排班表承载全院岗位（护士/技师/收费员的出勤排班
 * 也在这一张表），不加 {@code staff_type = 1} 会把出勤班算成出诊班。
 */
@Data
public class WorkbenchDoctorStatsRowVO implements Serializable {

    /**
     * 今日出诊排班数
     */
    private Long todayScheduleCount;

    /**
     * 候诊人数（排队状态 1候诊 2就诊中 3已叫号）
     */
    private Long waitingCount;

    /**
     * 我的住院患者数（在院且主治医师为我）
     */
    private Long myInpatientCount;

    /**
     * 待校对医嘱数
     */
    private Long todoVerifyOrderCount;

    /**
     * 待我会诊数
     */
    private Long todoConsultationCount;

    /**
     * 待处理危急值数（经检验单开单医生收敛）
     */
    private Long criticalValueCount;

    /**
     * 待归档病历数
     */
    private Long todoArchiveCount;
}