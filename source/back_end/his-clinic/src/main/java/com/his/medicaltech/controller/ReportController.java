package com.his.medicaltech.controller;

import com.his.common.base.Result;
import com.his.medicaltech.dto.ReportQueryDTO;
import com.his.medicaltech.dto.StatsQueryDTO;
import com.his.medicaltech.service.ReportService;
import com.his.medicaltech.service.StatsService;
import com.his.medicaltech.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报表统计控制器：报表统计页总览与各业务域按日期区间取数的接口入口。
 */
@Tag(name = "报表统计")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('patient:cdr:list', 'portal:workbench:view', 'qc:dataQuality:list', 'report:stats:list')")
public class ReportController {

    private final ReportService reportService;
    private final StatsService statsService;

    /**
     * 按日期区间一次聚合门诊、住院、收入、药事四组总盘数据。
     * 用于报表统计页顶部总览卡片的渲染。
     */
    @Operation(summary = "报表统计总览（门诊/住院/收入/药事，按日期区间聚合）")
    @PostMapping("/statsOverview")
    @PreAuthorize("hasAuthority('report:stats:list')")
    public Result<StatsOverviewVO> statsOverview(@RequestBody @Valid StatsQueryDTO queryDTO) {
        return Result.success(statsService.overview(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    /**
     * 按日期区间汇总门诊业务量。
     * 报表统计链路中门诊维度的独立取数出口。
     */
    @Operation(summary = "获取门诊统计")
    @PostMapping("/outpatientStats")
    public Result<OutpatientStatsVO> getOutpatientStats(@Valid @RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getOutpatientStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    /**
     * 按日期区间汇总收费域金额与笔数。
     * 报表统计链路中收入维度的独立取数出口。
     */
    @Operation(summary = "获取收费统计")
    @PostMapping("/chargeStats")
    public Result<ChargeStatsVO> getChargeStats(@Valid @RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getChargeStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    /**
     * 按日期区间汇总药事业务量。
     * 报表统计链路中药事维度的独立取数出口。
     */
    @Operation(summary = "获取药品统计")
    @PostMapping("/drugStats")
    public Result<DrugStatsVO> getDrugStats(@Valid @RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getDrugStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }

    /**
     * 按日期区间汇总医技检查与检验业务量。
     * 报表统计链路中医技维度的独立取数出口。
     */
    @Operation(summary = "获取医技统计")
    @PostMapping("/medicalTechStats")
    public Result<MedicalTechStatsVO> getMedicalTechStats(@Valid @RequestBody ReportQueryDTO queryDTO) {
        return Result.success(reportService.getMedicalTechStats(queryDTO.getStartDate(), queryDTO.getEndDate()));
    }
}
