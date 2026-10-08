package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.VteService;
import com.his.patient.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VTE 防控（sql/167，菜单 332 风险防控 / 333 院内监测）。
 */
@Tag(name = "VTE 防控")
@RestController
@RequestMapping("/patient/inpatient/vte")
@RequiredArgsConstructor
public class VteController {

    private final VteService vteService;

    @Operation(summary = "VTE 防控看板（在院中高危人数 / 未落实人数 / 本月院内新发）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @GetMapping("/overview")
    public Result<VteOverviewVO> overview() {
        return Result.success(vteService.overview());
    }

    @Operation(summary = "VTE 风险名单（默认只给中高危，风险取每次住院最新一条 Caprini 评估）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @PostMapping("/riskListPage")
    public Result<PageResult<VteRiskListVO>> riskListPage(@Valid @RequestBody VteRiskQueryPageDTO query) {
        return Result.success(vteService.riskListPage(query));
    }

    @Operation(summary = "预防措施记录分页")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @PostMapping("/preventListPage")
    public Result<PageResult<VtePreventVO>> preventListPage(@Valid @RequestBody VtePreventQueryPageDTO query) {
        return Result.success(vteService.preventListPage(query));
    }

    @Operation(summary = "某次住院的措施记录（按基础→物理→药物排序）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @GetMapping("/preventListByAdmission")
    public Result<List<VtePreventVO>> preventListByAdmission(@RequestParam Long admissionId) {
        return Result.success(vteService.preventListByAdmission(admissionId));
    }

    @Operation(summary = "措施项下拉（recommend 由风险等级算，前端不猜）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @GetMapping("/measureOptions")
    public Result<List<VteMeasureOptionVO>> measureOptions(@RequestParam(required = false) Integer riskLevel) {
        return Result.success(vteService.measureOptions(riskLevel));
    }

    @Operation(summary = "登记/落实 VTE 预防措施（风险等级与措施类别服务端补全）")
    @PreAuthorize("hasAuthority('nursing:vte:preventEdit')")
    @PostMapping("/preventUpsert")
    public Result<VtePreventVO> preventUpsert(@Valid @RequestBody VtePreventUpsertDTO dto) {
        return Result.success("措施已保存", vteService.preventUpsert(dto));
    }

    @Operation(summary = "删除措施记录")
    @PreAuthorize("hasAuthority('nursing:vte:preventDelete')")
    @DeleteMapping("/preventDeleteById")
    public Result<Integer> preventDeleteById(@RequestParam Long id) {
        return Result.success("已删除", vteService.preventDeleteById(id));
    }

    @Operation(summary = "VTE 事件分页")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @PostMapping("/eventListPage")
    public Result<PageResult<VteEventVO>> eventListPage(@Valid @RequestBody VteEventQueryPageDTO query) {
        return Result.success(vteService.eventListPage(query));
    }

    @Operation(summary = "某次住院的 VTE 事件")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @GetMapping("/eventListByAdmission")
    public Result<List<VteEventVO>> eventListByAdmission(@RequestParam Long admissionId) {
        return Result.success(vteService.eventListByAdmission(admissionId));
    }

    @Operation(summary = "登记/修改 VTE 事件（院内发生 vs 入院带入由登记人判定，服务端守住计入口径）")
    @PreAuthorize("hasAuthority('nursing:vte:eventEdit')")
    @PostMapping("/eventUpsert")
    public Result<VteEventVO> eventUpsert(@Valid @RequestBody VteEventUpsertDTO dto) {
        return Result.success("事件已登记", vteService.eventUpsert(dto));
    }

    @Operation(summary = "删除 VTE 事件")
    @PreAuthorize("hasAuthority('nursing:vte:eventEdit')")
    @DeleteMapping("/eventDeleteById")
    public Result<Integer> eventDeleteById(@RequestParam Long id) {
        return Result.success("已删除", vteService.eventDeleteById(id));
    }

    @Operation(summary = "实时试算（不落库；报数以快照为准）")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @GetMapping("/previewStats")
    public Result<VteStatsVO> previewStats(@RequestParam String statMonth) {
        return Result.success(vteService.previewStats(statMonth));
    }

    @Operation(summary = "生成/重算月度防控指标快照（同月同范围覆盖）")
    @PreAuthorize("hasAuthority('nursing:vte:statGenerate')")
    @PostMapping("/generateStats")
    public Result<List<VteStatsVO>> generateStats(@Valid @RequestBody VteStatsGenerateDTO dto) {
        List<VteStatsVO> rows = vteService.generateStats(dto);
        return Result.success("已生成 " + rows.size() + " 条快照", rows);
    }

    @Operation(summary = "已生成的防控指标分页")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @PostMapping("/statsListPage")
    public Result<PageResult<VteStatsVO>> statsListPage(@Valid @RequestBody VteStatsQueryPageDTO query) {
        return Result.success(vteService.statsListPage(query));
    }

    @Operation(summary = "导出防控指标 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('nursing:vte:statExport')")
    @PostMapping("/statsExportCsv")
    public Result<String> statsExportCsv(@Valid @RequestBody VteStatsQueryPageDTO query) {
        return Result.success(vteService.statsExportCsv(query));
    }
}
