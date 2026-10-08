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
 * 护理质控（菜单 334 / 路径 /nursing-qc）。
 */
@Tag(name = "护理质控")
@RestController
@RequestMapping("/nursing/qc")
@RequiredArgsConstructor
public class NursingQcController {

    private final NursingQcService nursingQcService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "病区下拉（只含当前岗位可见科室的启用病区，附床位）")
    @GetMapping("/wardSelectList")
    public Result<List<NurseQcVO.Ward>> wardSelectList(@RequestParam(required = false) String keyword) {
        return Result.success(nursingQcService.wardSelectList(keyword));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查人下拉（该病区所属科室的在职人员）")
    @GetMapping("/inspectorSelectList")
    public Result<List<NurseQcVO.Inspector>> inspectorSelectList(@RequestParam Long wardId,
                                                                 @RequestParam(required = false) String keyword) {
        return Result.success(nursingQcService.inspectorSelectList(wardId, keyword));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "检查项标准目录（category 空=全部启用项，检查单表单的行来源）")
    @GetMapping("/itemSelectList")
    public Result<List<NurseQcVO.ItemDef>> itemSelectList(@RequestParam(required = false) Integer category) {
        return Result.success(nursingQcService.itemSelectList(category));
    }

    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "检查单分页（病区 × 月 × 类别）")
    @PostMapping("/checkListPage")
    public Result<PageResult<NurseQcVO.CheckRow>> checkListPage(
            @Valid @RequestBody(required = false) NursingQcDTO.CheckQueryPage dto) {
        return Result.success(nursingQcService.checkListPage(dto));
    }

    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "检查单详情（主表 + 明细 + 目录，并给出本轮漏查项数）")
    @GetMapping("/getDetailById")
    public Result<NurseQcVO.CheckDetail> getDetailById(@RequestParam Long id) {
        return Result.success(nursingQcService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('nursing:qc:edit')")
    @Operation(summary = "保存检查单（主表六个汇总数字由明细求和，不接受前端传；已确认的单调拒绝）")
    @PostMapping("/checkUpsert")
    public Result<NurseQcVO.SaveResult> checkUpsert(@Valid @RequestBody NursingQcDTO.CheckUpsert dto) {
        return Result.success(nursingQcService.checkUpsert(dto));
    }

    @PreAuthorize("hasAuthority('nursing:qc:edit')")
    @Operation(summary = "检查单确认/退回（2-确认冻结明细，1-退回草稿才能改）")
    @PostMapping("/checkStatus")
    public Result<NurseQcVO.SaveResult> checkStatus(@Valid @RequestBody NursingQcDTO.CheckStatus dto) {
        return Result.success(nursingQcService.checkStatus(dto));
    }

    @PreAuthorize("hasAuthority('nursing:qc:delete')")
    @Operation(summary = "删除检查单（连同明细物理删：唯一键不含 del_flag）")
    @DeleteMapping("/checkDeleteById")
    public Result<Void> checkDeleteById(@RequestParam Long id) {
        nursingQcService.checkDeleteById(id);
        return Result.success("已删除", null);
    }

    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "月度 KPI 看板（四条指标永远都在，没有台账的显示未重算）")
    @PostMapping("/monthMetrics")
    public Result<List<NurseQcVO.Kpi>> monthMetrics(@Valid @RequestBody NursingQcDTO.MonthQuery dto) {
        return Result.success(nursingQcService.monthMetrics(dto));
    }

    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "指标趋势（一条指标按月一个点，wardId 空=可见范围全院合并）")
    @PostMapping("/trend")
    public Result<List<NurseQcVO.Kpi>> trend(@Valid @RequestBody NursingQcDTO.TrendQuery dto) {
        return Result.success(nursingQcService.trend(dto));
    }

    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "病区对比（某月某指标各病区落点，按指标值倒序）")
    @PostMapping("/wardCompare")
    public Result<List<NurseQcVO.LedgerRow>> wardCompare(@Valid @RequestBody NursingQcDTO.CompareQuery dto) {
        return Result.success(nursingQcService.wardCompare(dto));
    }

    @PreAuthorize("hasAuthority('nursing:qc:list')")
    @Operation(summary = "指标台账分页（每行都带分子分母与来源备注）")
    @PostMapping("/ledgerListPage")
    public Result<PageResult<NurseQcVO.LedgerRow>> ledgerListPage(
            @Valid @RequestBody(required = false) NursingQcDTO.LedgerQueryPage dto) {
        return Result.success(nursingQcService.ledgerListPage(dto));
    }

    @PreAuthorize("hasAuthority('nursing:qc:calc')")
    @Operation(summary = "重算台账（已上报的行由 SQL 侧闸门跳过，不静默改历史数字）")
    @PostMapping("/recalc")
    public Result<NurseQcVO.RecalcResult> recalc(@Valid @RequestBody NursingQcDTO.RecalcCommand dto) {
        NurseQcVO.RecalcResult result = nursingQcService.recalc(dto);
        return Result.success(result.getMessage(), result);
    }

    @PreAuthorize("hasAuthority('nursing:qc:calc')")
    @Operation(summary = "上报 / 退回（2-锁定该月台账，1-退回后重算才会覆盖）")
    @PostMapping("/report")
    public Result<NurseQcVO.ReportResult> report(@Valid @RequestBody NursingQcDTO.ReportCommand dto) {
        NurseQcVO.ReportResult result = nursingQcService.report(dto);
        return Result.success(result.getMessage(), result);
    }

    @PreAuthorize("hasAuthority('nursing:qc:delete')")
    @Operation(summary = "删除台账行（物理删，删掉再重算即可）")
    @DeleteMapping("/ledgerDeleteById")
    public Result<Void> ledgerDeleteById(@RequestParam Long id) {
        nursingQcService.ledgerDeleteById(id);
        return Result.success("已删除", null);
    }
}
