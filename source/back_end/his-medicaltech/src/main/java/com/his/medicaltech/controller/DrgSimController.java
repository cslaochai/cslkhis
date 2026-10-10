package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.DrgSimDTO;
import com.his.medicaltech.service.DrgSimService;
import com.his.medicaltech.vo.DrgSimVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DRG-DIP 分组模拟控制器（院内简化模拟器；正式方案接入后整体替换）。
 */
@Tag(name = "DRG-DIP 分组模拟")
@RestController
@RequestMapping("/report/drg")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:drg:list')")
public class DrgSimController {

    private final DrgSimService drgSimService;

    @Operation(summary = "单条模拟（首页主诊断为空时补传 icdCode）")
    @PostMapping("/simulate")
    public Result<DrgSimVO.SimResult> simulate(@Valid @RequestBody DrgSimDTO.Simulate dto) {
        return Result.success("模拟完成", drgSimService.simulate(dto));
    }

    @Operation(summary = "批量模拟（不传 ids 默认最近 50 条）")
    @PostMapping("/simulateBatch")
    public Result<DrgSimVO.SummaryListVO> simulateBatch(@Valid @RequestBody DrgSimDTO.SimulateBatch dto) {
        return Result.success("批量模拟完成", drgSimService.simulateBatch(dto));
    }

    @Operation(summary = "模拟结果分页")
    @PostMapping("/result/listPage")
    public Result<PageResult<DrgSimVO.ResultRow>> resultPage(@Valid @RequestBody DrgSimDTO.ResultQuery dto) {
        var page = drgSimService.resultPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "可模拟首页列表 + 入组/盈亏统计")
    @GetMapping("/summary/list")
    public Result<DrgSimVO.SummaryListVO> summaryList(@RequestParam(required = false) Integer limit) {
        return Result.success(drgSimService.summaryList(limit, null));
    }

    @Operation(summary = "组表（sys_drg_group）")
    @GetMapping("/group/list")
    public Result<List<DrgSimVO.GroupRow>> groupList() {
        return Result.success(drgSimService.groupList());
    }
}
