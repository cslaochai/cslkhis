package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.AntibioticStatsGenerateDTO;
import com.his.pharmacy.dto.AntibioticStatsPageQueryDTO;
import com.his.pharmacy.dto.IncisionReviewPageQueryDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抗菌药物使用监测（使用率 / 使用强度 AUD / 微生物送检率）+ I 类切口预防用药点评。
 */
@Tag(name = "抗菌药物使用监测与I类切口点评")
@RestController
@RequestMapping("/antibioticMonitor")
@RequiredArgsConstructor
public class AntibioticMonitorController {

    private final AntibioticMonitorService antibioticMonitorService;

    /**
     * 查已归档的月度抗菌药物监测指标快照，可按统计月份与统计范围筛。
     * 用于抗菌药物使用监测页的指标台账表格。
     */
    @Operation(summary = "已生成的监测指标分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:monitor')")
    @PostMapping("/statsListPage")
    public Result<PageResult<AntibioticStatsVO>> statsListPage(@Valid @RequestBody AntibioticStatsPageQueryDTO query) {
        return Result.success(antibioticMonitorService.listPage(query));
    }

    /**
     * 按月实时试算使用率 / 使用强度 / 微生物送检率，只回结果不落库。
     * 用于监测页顶部「当前试算」区，报数仍以已生成快照为准。
     */
    @Operation(summary = "实时试算（不落库；报数以快照为准）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:monitor')")
    @GetMapping("/previewStats")
    public Result<AntibioticStatsVO> previewStats(@RequestParam String statMonth) {
        return Result.success(antibioticMonitorService.previewStats(statMonth));
    }

    /**
     * 按统计月份与统计范围算出全院/科室的监测指标并写成快照，同月同范围重算覆盖旧行。
     * 用于监测页的生成快照动作，月结报数前由药士手工触发一次。
     */
    @Operation(summary = "生成/重算月度监测快照（同月同范围覆盖）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:statGenerate')")
    @PostMapping("/generateStats")
    public Result<List<AntibioticStatsVO>> generateStats(@Valid @RequestBody AntibioticStatsGenerateDTO dto) {
        return Result.success(antibioticMonitorService.generateStats(dto));
    }

    /**
     * 把与当前查询条件一致的监测指标导出为 CSV 文本（带上限截断）。
     * 用于监测页导出按钮，供等级评审与专项整治报送留档。
     */
    @Operation(summary = "导出监测指标 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:statExport')")
    @PostMapping("/statsExportCsv")
    public Result<String> statsExportCsv(@Valid @RequestBody AntibioticStatsPageQueryDTO query) {
        return Result.success(antibioticMonitorService.statsExportCsv(query));
    }

    /**
     * 列出尚未点评的 I 类切口手术及其围手术期抗菌药物医嘱证据。
     * 用于 I 类切口点评页的待点评页签，药师逐台点开填结论。
     */
    @Operation(summary = "待点评的 I 类切口手术（含围手术期抗菌药物医嘱证据）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:incision')")
    @GetMapping("/incisionCandidates")
    public Result<List<IncisionCandidateVO>> incisionCandidates() {
        return Result.success(antibioticMonitorService.incisionCandidates());
    }

    /**
     * 已提交的 I 类切口预防用药点评记录分页。
     * 用于 I 类切口点评页的点评记录页签。
     */
    @Operation(summary = "I 类切口点评记录分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:incision')")
    @PostMapping("/incisionReviewListPage")
    public Result<PageResult<IncisionReviewVO>> incisionReviewListPage(@Valid @RequestBody IncisionReviewPageQueryDTO query) {
        return Result.success(antibioticMonitorService.incisionReviewListPage(query));
    }

    /**
     * 落一条 I 类切口预防用药点评结论：无ID为首次点评，带ID为对同一台手术重评覆盖。
     * 用于点评页待点评/点评记录两个页签共用的点评弹框保存。
     */
    @Operation(summary = "提交/重评 I 类切口预防用药点评结论")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:incisionReview')")
    @PostMapping("/incisionReviewUpsert")
    public Result<IncisionReviewVO> incisionReviewUpsert(@Valid @RequestBody IncisionReviewUpsertDTO dto) {
        return Result.success(antibioticMonitorService.incisionReviewUpsert(dto));
    }
}
