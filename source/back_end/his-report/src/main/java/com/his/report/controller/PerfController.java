package com.his.report.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.report.dto.PerfDTO;
import com.his.report.service.PerfService;
import com.his.report.vo.PerfVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 绩效成本核算控制器。
 *
 * <p>口径：收入=收费明细月度净额；药占比=药费/收入；结余=收入-成本；
 * 绩效=max(0,结余)×提成系数。成本同科室同月唯一（拒绝重复录入），核算结果重算覆盖。
 */
@Tag(name = "绩效与成本核算")
@RestController
@RequestMapping("/report/perf")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:perf:list')")
public class PerfController {

    private final PerfService perfService;

    @PreAuthorize("hasAuthority('report:perf:add')")
    @Operation(summary = "科室月度成本录入（同科室同月唯一，重复拒绝）")
    @PostMapping("/cost/save")
    public Result<PerfVO.CostRow> saveCost(@Valid @RequestBody PerfDTO.CostSave dto) {
        return Result.success("成本已录入", perfService.saveCost(dto));
    }

    @Operation(summary = "成本分页")
    @PostMapping("/cost/listPage")
    public Result<PageResult<PerfVO.CostRow>> costPage(@RequestBody PerfDTO.CostQuery dto) {
        var page = perfService.costPage(dto == null ? new PerfDTO.CostQuery() : dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "核算预览（收入聚合 + 成本快照）")
    @GetMapping("/revenueInfo")
    public Result<PerfVO.RevenueInfo> revenueInfo(@RequestParam Long deptId, @RequestParam String costMonth) {
        return Result.success(perfService.revenueInfo(deptId, costMonth));
    }

    @Operation(summary = "执行核算（重算覆盖）")
    @PostMapping("/calc")
    public Result<PerfVO.PerfRow> calc(@Valid @RequestBody PerfDTO.PerfCalc dto) {
        return Result.success("核算完成", perfService.calc(dto));
    }

    @Operation(summary = "绩效结果分页")
    @PostMapping("/result/listPage")
    public Result<PageResult<PerfVO.PerfRow>> perfPage(@RequestBody PerfDTO.PerfQuery dto) {
        var page = perfService.perfPage(dto == null ? new PerfDTO.PerfQuery() : dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }
}
