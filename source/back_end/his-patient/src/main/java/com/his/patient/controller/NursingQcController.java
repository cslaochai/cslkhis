package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.NursingQcDTO;
import com.his.patient.service.NursingQcService;
import com.his.patient.vo.NurseQcVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 护理质控
 */
@Tag(name = "护理质控")
@RestController
@RequestMapping("/nursing/qc")
@RequiredArgsConstructor
public class NursingQcController {

    private final NursingQcService nursingQcService;

    /**
     * 取当前岗位可见科室下的启用病区及其开放与占用床位数。
     * 用于护理质控页查询卡的病区下拉，看板、检查表、台账三个页签共用同一份候选。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "病区下拉（只含当前岗位可见科室的启用病区，附床位）")
    @GetMapping("/wardSelectList")
    public Result<List<NurseQcVO.Ward>> wardSelectList(@RequestParam(required = false) String keyword) {
        return Result.success(nursingQcService.wardSelectList(keyword));
    }

    /**
     * 按病区取其所属科室的在职人员，作为检查人候选。
     * 用于检查单新增/修改弹框里选定病区后的检查人下拉。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查人下拉（该病区所属科室的在职人员）")
    @GetMapping("/inspectorSelectList")
    public Result<List<NurseQcVO.Inspector>> inspectorSelectList(@RequestParam Long wardId,
                                                                 @RequestParam(required = false) String keyword) {
        return Result.success(nursingQcService.inspectorSelectList(wardId, keyword));
    }

    /**
     * 取指定类别的启用检查项标准目录（含满分与关键项标记）。
     * 用于检查单弹框铺评分行：已录明细按项回填，未录的留空不提交。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查项标准目录（category 空=全部启用项，检查单表单的行来源）")
    @GetMapping("/itemSelectList")
    public Result<List<NurseQcVO.ItemDef>> itemSelectList(@RequestParam(required = false) Integer category) {
        return Result.success(nursingQcService.itemSelectList(category));
    }

    /**
     * 按病区、月份、类别分页查询护理检查单。
     * 用于护理质控页「检查表（评分）」页签的查询卡与表格。
     */
    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "检查单分页（病区 × 月 × 类别）")
    @PostMapping("/checkListPage")
    public Result<PageResult<NurseQcVO.CheckRow>> checkListPage(
            @Valid @RequestBody NursingQcDTO.CheckPageQueryDTO dto) {
        return Result.success(nursingQcService.checkListPage(dto));
    }

    /**
     * 取一张检查单的主表、已录明细与本轮漏查项数。
     * 检查单详情弹框按行 id 自拉，修改弹框打开时也用它回显。
     */
    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "检查单详情（主表 + 明细 + 目录，并给出本轮漏查项数）")
    @GetMapping("/getDetailById")
    public Result<NurseQcVO.CheckDetail> getDetailById(@RequestParam Long id) {
        return Result.success(nursingQcService.getDetailById(id));
    }

    /**
     * 新增或修改一张检查单连同逐项明细，主表汇总数字一律由明细求和得出。
     * 用于检查单弹框保存；已确认的单据会被拒绝，需先退回草稿。
     */
    @PreAuthorize("hasAuthority('nursing:qc:edit')")
    @Operation(summary = "保存检查单（主表六个汇总数字由明细求和，不接受前端传；已确认的单调拒绝）")
    @PostMapping("/checkUpsert")
    public Result<NurseQcVO.SaveResult> checkUpsert(@Valid @RequestBody NursingQcDTO.CheckUpsertDTO dto) {
        return Result.success(nursingQcService.checkUpsert(dto));
    }

    /**
     * 在确认与退回草稿之间流转检查单状态，决定明细还能不能改。
     * 用于检查表页签行操作「确认」「退回草稿」的二次确认之后。
     */
    @PreAuthorize("hasAuthority('nursing:qc:edit')")
    @Operation(summary = "检查单确认/退回（2-确认冻结明细，1-退回草稿才能改）")
    @PostMapping("/checkStatus")
    public Result<NurseQcVO.SaveResult> checkStatus(@Valid @RequestBody NursingQcDTO.CheckStatus dto) {
        return Result.success(nursingQcService.checkStatus(dto));
    }

    /**
     * 删除一张检查单并连带清除它的全部明细行。
     * 用于检查表页签行操作「删除」的二次确认之后。
     */
    @PreAuthorize("hasAuthority('nursing:qc:delete')")
    @Operation(summary = "删除检查单（连同明细物理删：唯一键不含 del_flag）")
    @DeleteMapping("/checkDeleteById")
    public Result<Void> checkDeleteById(@RequestParam Long id) {
        nursingQcService.checkDeleteById(id);
        return Result.success("已删除", null);
    }

    /**
     * 取指定月份病区（病区为空则全院）各项护理质量指标的当期值与目标差距。
     * 用于护理质控页「质量看板」页签顶部的 KPI 卡片。
     */
    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "月度 KPI 看板（四条指标永远都在，没有台账的显示未重算）")
    @PostMapping("/monthMetrics")
    public Result<List<NurseQcVO.Kpi>> monthMetrics(@Valid @RequestBody NursingQcDTO.MonthQueryDTO dto) {
        return Result.success(nursingQcService.monthMetrics(dto));
    }

    /**
     * 取一条指标连续若干月的取值点，病区为空时按岗位可见范围合并。
     * 用于质量看板页签的指标趋势折线图。
     */
    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "指标趋势（一条指标按月一个点，wardId 空=可见范围全院合并）")
    @PostMapping("/trend")
    public Result<List<NurseQcVO.Kpi>> trend(@Valid @RequestBody NursingQcDTO.TrendQueryDTO dto) {
        return Result.success(nursingQcService.trend(dto));
    }

    /**
     * 取某月某指标在各病区的落点并按指标值倒序。
     * 用于质量看板页签的病区对比列表。
     */
    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "病区对比（某月某指标各病区落点，按指标值倒序）")
    @PostMapping("/wardCompare")
    public Result<List<NurseQcVO.LedgerRow>> wardCompare(@Valid @RequestBody NursingQcDTO.CompareQueryDTO dto) {
        return Result.success(nursingQcService.wardCompare(dto));
    }

    /**
     * 分页查询月度护理质量指标台账，每行带分子分母与来源说明。
     * 用于护理质控页「指标台账」页签的查询卡与表格。
     */
    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "指标台账分页（每行都带分子分母与来源备注）")
    @PostMapping("/ledgerListPage")
    public Result<PageResult<NurseQcVO.LedgerRow>> ledgerListPage(
            @Valid @RequestBody NursingQcDTO.LedgerPageQueryDTO dto) {
        return Result.success(nursingQcService.ledgerListPage(dto));
    }

    /**
     * 按月份与病区重算指标台账，已上报的行不覆盖。
     * 用于指标台账页签「重算」按钮，重算后看板同步刷新。
     */
    @PreAuthorize("hasAuthority('nursing:qc:calc')")
    @Operation(summary = "重算台账（已上报的行由 SQL 侧闸门跳过，不静默改历史数字）")
    @PostMapping("/recalc")
    public Result<NurseQcVO.RecalcResult> recalc(@Valid @RequestBody NursingQcDTO.RecalcCommandDTO dto) {
        NurseQcVO.RecalcResult result = nursingQcService.recalc(dto);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 整月上报锁定或退回未上报，决定该月台账还允不允许被重算覆盖。
     * 用于指标台账页签「上报锁定」「退回未上报」的二次确认之后。
     */
    @PreAuthorize("hasAuthority('nursing:qc:calc')")
    @Operation(summary = "上报 / 退回（2-锁定该月台账，1-退回后重算才会覆盖）")
    @PostMapping("/report")
    public Result<NurseQcVO.ReportResult> report(@Valid @RequestBody NursingQcDTO.ReportCommandDTO dto) {
        NurseQcVO.ReportResult result = nursingQcService.report(dto);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 删除一条月度指标台账行。
     * 用于台账页签行操作「删除」：算错的行删掉后重新重算即可。
     */
    @PreAuthorize("hasAuthority('nursing:qc:delete')")
    @Operation(summary = "删除台账行（物理删，删掉再重算即可）")
    @DeleteMapping("/ledgerDeleteById")
    public Result<Void> ledgerDeleteById(@RequestParam Long id) {
        nursingQcService.ledgerDeleteById(id);
        return Result.success("已删除", null);
    }
}
