package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.AntibioticStatsGenerateDTO;
import com.his.pharmacy.dto.AntibioticStatsQueryPageDTO;
import com.his.pharmacy.dto.IncisionReviewQueryPageDTO;
import com.his.pharmacy.dto.IncisionReviewUpsertDTO;
import com.his.pharmacy.service.AntibioticMonitorService;
import com.his.pharmacy.vo.AntibioticStatsVO;
import com.his.pharmacy.vo.IncisionCandidateVO;
import com.his.pharmacy.vo.IncisionReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 抗菌药物使用监测（使用率 / 使用强度 AUD / 微生物送检率）+ I 类切口预防用药点评。
 *
 * <p>权限：监测页读 {@code pharmacy:antibiotic:monitor}；生成快照 {@code :statGenerate}；
 * 导出 {@code :statExport}；I类切口点评页读 {@code pharmacy:antibiotic:incision}；
 * 提交结论 {@code :incisionReview}。
 */
@Tag(name = "抗菌药物使用监测与I类切口点评")
@RestController
@RequestMapping("/antibioticMonitor")
@RequiredArgsConstructor
public class AntibioticMonitorController {

    private final AntibioticMonitorService monitorService;

    @Operation(summary = "已生成的监测指标分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:monitor')")
    @PostMapping("/statsListPage")
    public Result<PageResult<AntibioticStatsVO>> statsListPage(@RequestBody AntibioticStatsQueryPageDTO query) {
        return Result.success(monitorService.statsListPage(query));
    }

    @Operation(summary = "实时试算（不落库；报数以快照为准）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:monitor')")
    @GetMapping("/previewStats")
    public Result<AntibioticStatsVO> previewStats(@RequestParam String statMonth) {
        return Result.success(monitorService.previewStats(statMonth));
    }

    @Operation(summary = "生成/重算月度监测快照（同月同范围覆盖）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:statGenerate')")
    @PostMapping("/generateStats")
    public Result<List<AntibioticStatsVO>> generateStats(@Valid @RequestBody AntibioticStatsGenerateDTO dto) {
        return Result.success(monitorService.generateStats(dto));
    }

    @Operation(summary = "导出监测指标 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:statExport')")
    @PostMapping("/statsExportCsv")
    public Result<String> statsExportCsv(@RequestBody AntibioticStatsQueryPageDTO query) {
        return Result.success(monitorService.statsExportCsv(query));
    }

    @Operation(summary = "待点评的 I 类切口手术（含围手术期抗菌药物医嘱证据）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:incision')")
    @GetMapping("/incisionCandidates")
    public Result<List<IncisionCandidateVO>> incisionCandidates() {
        return Result.success(monitorService.incisionCandidates());
    }

    @Operation(summary = "I 类切口点评记录分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:incision')")
    @PostMapping("/incisionReviewListPage")
    public Result<PageResult<IncisionReviewVO>> incisionReviewListPage(@RequestBody IncisionReviewQueryPageDTO query) {
        return Result.success(monitorService.incisionReviewListPage(query));
    }

    @Operation(summary = "提交/重评 I 类切口预防用药点评结论")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:incisionReview')")
    @PostMapping("/incisionReviewUpsert")
    public Result<IncisionReviewVO> incisionReviewUpsert(@Valid @RequestBody IncisionReviewUpsertDTO dto) {
        return Result.success(monitorService.incisionReviewUpsert(dto));
    }
}
