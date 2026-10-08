package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.PerfDTO;
import com.his.medicaltech.service.PerfService;
import com.his.medicaltech.vo.PerfVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 绩效成本核算控制器。
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
    public Result<PageResult<PerfVO.CostRow>> costPage(@Valid @RequestBody PerfDTO.CostQuery dto) {
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
    public Result<PageResult<PerfVO.PerfRow>> perfPage(@Valid @RequestBody PerfDTO.PerfQuery dto) {
        var page = perfService.perfPage(dto == null ? new PerfDTO.PerfQuery() : dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }
}
