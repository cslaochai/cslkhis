package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班周总览（只读驾驶舱）：门诊号源、在岗人次、人力缺口、总值班一屏聚合。
 * 数据全部来自既有事实层（biz_staff_schedule / biz_schedule / biz_duty_roster / biz_staff_plan_rule），
 * 这里只做聚合与装配，不产生任何新事实。
 */
@Data
public class ScheduleOverviewVO {

    /**
     * 周起始（周一）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 天数（固定 7）
     */
    private Integer days;

    /**
     * 岗位类别 × 日期在岗人次（卡片行）
     */
    private List<OverviewStaffTypeDayVO> staffTypeDays;

    /**
     * 出诊单元 × 日期在岗人次（矩阵格子）
     */
    private List<OverviewUnitDayVO> unitDays;

    /**
     * 门诊号源按日汇总
     */
    private List<OverviewClinicDayVO> clinicDays;

    /**
     * 人力缺口清单
     */
    private List<OverviewShortfallVO> shortfalls;

    /**
     * 本周每日总值班解析（白班/夜班）
     */
    private List<OverviewDutyDayVO> dutyDays;
}
