package com.his.appoint.controller;

import com.his.appoint.service.ScheduleOverviewService;
import com.his.appoint.vo.ScheduleOverviewVO;
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

@Tag(name = "排班总览")
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleOverviewController {

    private final ScheduleOverviewService scheduleOverviewService;

    @PreAuthorize("hasAuthority('org:schedule:list')")
    @Operation(summary = "排班周总览（门诊号源/在岗/缺口/总值班一屏聚合，只读）")
    @GetMapping("/overviewWeek")
    public Result<ScheduleOverviewVO> overviewWeek(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beginDate) {
        return Result.success(scheduleOverviewService.overviewWeek(beginDate));
    }
}
