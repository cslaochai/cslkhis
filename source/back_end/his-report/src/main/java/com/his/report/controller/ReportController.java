package com.his.report.controller;

import com.his.common.base.Result;
import com.his.report.dto.ReportQueryDTO;
import com.his.report.service.ReportService;
import com.his.report.dto.StatsQueryDTO;
import com.his.report.service.StatsService;
import com.his.report.vo.StatsOverviewVO;
import com.his.report.vo.ChargeStatsVO;
import com.his.report.vo.DrugStatsVO;
import com.his.report.vo.MedicalTechStatsVO;
import com.his.report.vo.OutpatientStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 报表统计控制器
 */
@Tag(name = "报表统计")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('patient:cdr:list', 'portal:workbench:view', 'qc:dataQuality:list', 'report:stats:list')")
public class ReportController {

    private final ReportService reportService;
    private final StatsService statsService;

    @Operation(summary = "报表统计总览（门诊/住院/收入/药事，按日期区间聚合）")
    @PostMapping("/statsOverview")
    @PreAuthorize("hasAuthority('report:stats:list')")
    public Result<StatsOverviewVO> statsOverview(@RequestBody @Valid StatsQueryDTO queryDTO) {
        return Result.success(statsService.overview(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    @Operation(summary = "获取门诊统计")
    @PostMapping("/outpatientStats")
    public Result<OutpatientStatsVO> getOutpatientStats(@RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getOutpatientStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    @Operation(summary = "获取收费统计")
    @PostMapping("/chargeStats")
    public Result<ChargeStatsVO> getChargeStats(@RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getChargeStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    @Operation(summary = "获取药品统计")
    @PostMapping("/drugStats")
    public Result<DrugStatsVO> getDrugStats(@RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getDrugStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    @Operation(summary = "获取医技统计")
    @PostMapping("/medicalTechStats")
    public Result<MedicalTechStatsVO> getMedicalTechStats(@RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getMedicalTechStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }
}
