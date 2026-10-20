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
 * VTE 防控控制器：住院患者静脉血栓风险名单与预防措施落实、院内事件监测及月度指标报数的接口入口。
 */
@Tag(name = "VTE 防控")
@RestController
@RequestMapping("/patient/inpatient/vte")
@RequiredArgsConstructor
public class VteController {

    private final VteService vteService;

    /**
     * 防控看板汇总：在院中高危人数、未落实人数、本月院内新发。
     * 用于「VTE 风险防控」页顶部统计卡。
     */
    @Operation(summary = "VTE 防控看板（在院中高危人数 / 未落实人数 / 本月院内新发）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @GetMapping("/overview")
    public Result<VteOverviewVO> overview() {
        return Result.success(vteService.overview());
    }

    /**
     * 风险名单分页：默认只给中高危，风险等级取每次住院最新一次评估结果。
     * 用于「VTE 风险防控」页中高危名单查询卡。
     */
    @Operation(summary = "VTE 风险名单（默认只给中高危，风险取每次住院最新一条 Caprini 评估）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @PostMapping("/riskListPage")
    public Result<PageResult<VteRiskListVO>> riskListPage(@Valid @RequestBody VteRiskPageQueryDTO query) {
        return Result.success(vteService.riskListPage(query));
    }

    /**
     * 预防措施记录分页。
     * 供按科室与时间维度汇总核查措施落实情况时取数。
     */
    @Operation(summary = "预防措施记录分页")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @PostMapping("/preventListPage")
    public Result<PageResult<VtePreventVO>> preventListPage(@Valid @RequestBody VtePreventPageQueryDTO query) {
        return Result.success(vteService.preventListPage(query));
    }

    /**
     * 某次住院名下的措施记录，按基础、物理、药物顺序排列。
     * 用于「VTE 风险防控」页患者措施详情弹框。
     */
    @Operation(summary = "某次住院的措施记录（按基础→物理→药物排序）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @GetMapping("/preventListByAdmission")
    public Result<List<VtePreventVO>> preventListByAdmission(@RequestParam Long admissionId) {
        return Result.success(vteService.preventListByAdmission(admissionId));
    }

    /**
     * 措施项下拉：推荐标记由风险等级算出，前端不自行猜。
     * 用于新增措施弹框选措施，风险等级变化时重新取数。
     */
    @Operation(summary = "措施项下拉（recommend 由风险等级算，前端不猜）")
    @PreAuthorize("hasAuthority('nursing:vte:prevent')")
    @GetMapping("/measureOptions")
    public Result<List<VteMeasureOptionVO>> measureOptions(@RequestParam(required = false) Integer riskLevel) {
        return Result.success(vteService.measureOptions(riskLevel));
    }

    /**
     * 登记或修改预防措施：风险等级与措施类别由服务端补全。
     * 用于「VTE 风险防控」页新增措施弹框提交。
     */
    @Operation(summary = "登记/落实 VTE 预防措施（风险等级与措施类别服务端补全）")
    @PreAuthorize("hasAuthority('nursing:vte:preventEdit')")
    @PostMapping("/preventUpsert")
    public Result<VtePreventVO> preventUpsert(@Valid @RequestBody VtePreventUpsertDTO dto) {
        return Result.success("措施已保存", vteService.preventUpsert(dto));
    }

    /**
     * 删除措施记录。
     * 用于「VTE 风险防控」页措施清单行内删除。
     */
    @Operation(summary = "删除措施记录")
    @PreAuthorize("hasAuthority('nursing:vte:preventDelete')")
    @DeleteMapping("/preventDeleteById")
    public Result<Integer> preventDeleteById(@RequestParam Long id) {
        return Result.success("已删除", vteService.preventDeleteById(id));
    }

    /**
     * 院内 VTE 事件分页。
     * 用于「院内 VTE 监测」页事件登记区查询卡。
     */
    @Operation(summary = "VTE 事件分页")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @PostMapping("/eventListPage")
    public Result<PageResult<VteEventVO>> eventListPage(@Valid @RequestBody VteEventPageQueryDTO query) {
        return Result.success(vteService.eventListPage(query));
    }

    /**
     * 某次住院名下的 VTE 事件。
     * 供事件核实时按住院次回看已登记事件。
     */
    @Operation(summary = "某次住院的 VTE 事件")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @GetMapping("/eventListByAdmission")
    public Result<List<VteEventVO>> eventListByAdmission(@RequestParam Long admissionId) {
        return Result.success(vteService.eventListByAdmission(admissionId));
    }

    /**
     * 登记或修改 VTE 事件：院内发生与入院带入由登记人判定，服务端守住计入口径。
     * 用于「院内 VTE 监测」页事件登记弹框提交。
     */
    @Operation(summary = "登记/修改 VTE 事件（院内发生 vs 入院带入由登记人判定，服务端守住计入口径）")
    @PreAuthorize("hasAuthority('nursing:vte:eventEdit')")
    @PostMapping("/eventUpsert")
    public Result<VteEventVO> eventUpsert(@Valid @RequestBody VteEventUpsertDTO dto) {
        return Result.success("事件已登记", vteService.eventUpsert(dto));
    }

    /**
     * 删除 VTE 事件。
     * 供误登记事件纠正后清理。
     */
    @Operation(summary = "删除 VTE 事件")
    @PreAuthorize("hasAuthority('nursing:vte:eventEdit')")
    @DeleteMapping("/eventDeleteById")
    public Result<Integer> eventDeleteById(@RequestParam Long id) {
        return Result.success("已删除", vteService.eventDeleteById(id));
    }

    /**
     * 月度防控指标实时试算：不落库，报数以已生成的快照为准。
     * 用于「院内 VTE 监测」页实时试算卡。
     *
     * @param statMonth 统计月份（yyyy-MM）
     */
    @Operation(summary = "实时试算（不落库；报数以快照为准）")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @GetMapping("/previewStats")
    public Result<VteStatsVO> previewStats(@RequestParam String statMonth) {
        return Result.success(vteService.previewStats(statMonth));
    }

    /**
     * 生成或重算月度防控指标快照：同月同范围覆盖旧快照。
     * 用于「院内 VTE 监测」页按月报数前的快照生成。
     */
    @Operation(summary = "生成/重算月度防控指标快照（同月同范围覆盖）")
    @PreAuthorize("hasAuthority('nursing:vte:statGenerate')")
    @PostMapping("/generateStats")
    public Result<List<VteStatsVO>> generateStats(@Valid @RequestBody VteStatsGenerateDTO dto) {
        List<VteStatsVO> rows = vteService.generateStats(dto);
        return Result.success("已生成 " + rows.size() + " 条快照", rows);
    }

    /**
     * 已生成的月度指标快照分页。
     * 用于「院内 VTE 监测」页报数台账查询卡。
     */
    @Operation(summary = "已生成的防控指标分页")
    @PreAuthorize("hasAuthority('nursing:vte:monitor')")
    @PostMapping("/statsListPage")
    public Result<PageResult<VteStatsVO>> statsListPage(@Valid @RequestBody VteStatsPageQueryDTO query) {
        return Result.success(vteService.statsListPage(query));
    }

    /**
     * 导出防控指标文本，单次有行数上限。
     * 用于「院内 VTE 监测」页报数台账的导出。
     */
    @Operation(summary = "导出防控指标 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('nursing:vte:statExport')")
    @PostMapping("/statsExportCsv")
    public Result<String> statsExportCsv(@Valid @RequestBody VteStatsPageQueryDTO query) {
        return Result.success(vteService.statsExportCsv(query));
    }
}
