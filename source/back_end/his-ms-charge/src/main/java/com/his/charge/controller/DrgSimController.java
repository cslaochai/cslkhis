package com.his.charge.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.charge.config.DrgSchemeConfig;
import com.his.charge.dto.DrgSimDTO;
import com.his.charge.service.DrgSimService;
import com.his.charge.vo.DrgSimVO;
import com.his.charge.vo.DrgVersionVO;
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

    private final DrgSchemeConfig drgSchemeConfig;

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
    public Result<DrgSimVO.SummaryListVO> summaryList(@RequestParam(required = false) Integer limit,
                                                     @RequestParam(required = false) String version) {
        return Result.success(drgSimService.summaryList(limit, version, null));
    }

    @Operation(summary = "组表（biz_drg_group）")
    @GetMapping("/group/list")
    public Result<List<DrgSimVO.GroupRow>> groupList(@RequestParam(required = false) String version) {
        return Result.success(drgSimService.groupList(version));
    }

    @PreAuthorize("hasAnyAuthority('system:config:add')")
    @Operation(summary = "当前 DRG 结算默认版本")
    @GetMapping("/version")
    public Result<DrgVersionVO> drgVersion() {
        DrgVersionVO vo = new DrgVersionVO();
        vo.setDbVersion(drgSchemeConfig.getCurrenttVersion());
        vo.setRedisKey(DrgSchemeConfig.REDIS_KEY);
        return Result.success(vo);
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "设置 DRG 结算默认版本")
    @PostMapping("/version")
    public Result<Void> setDrgVersion(@RequestParam String version) {
        drgSchemeConfig.updateDefaultVersion(version);
        return Result.success("已切换 DRG 结算默认版本为 " + version, null);
    }
}
