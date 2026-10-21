package com.his.outpatient.controller;

import com.his.outpatient.service.BizClinicSourceOverviewService;
import com.his.outpatient.vo.ScheduleOverviewVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 排班总览控制器：一周排班事实的只读聚合查询接口入口。
 */
@Tag(name = "排班总览")
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleOverviewController {

    private final BizClinicSourceOverviewService bizScheduleOverviewService;

    /**
     * 排班周总览：把门诊号源、在岗人次、人力缺口、每日总值班按周聚合成一份只读数据。
     * 用于「排班总览」驾驶舱页面按周查看，切换周时重新取数。
     */
    @PreAuthorize("hasAuthority('schedule:overview:list')")
    @Operation(summary = "排班周总览（门诊号源/在岗/缺口/总值班一屏聚合，只读）")
    @GetMapping("/overviewWeek")
    public Result<ScheduleOverviewVO> overviewWeek(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beginDate) {
        return Result.success(bizScheduleOverviewService.overviewWeek(beginDate));
    }
}