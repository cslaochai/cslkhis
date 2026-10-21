package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.LisQcDTO;
import com.his.medicaltech.service.LisQcService;
import com.his.medicaltech.vo.LisQcVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * LIS 室内质控控制器：质控计划维护、质控结果录入与 Westgard 判定、失控处理与复核的接口入口。
 */
@Tag(name = "LIS室内质控")
@RestController
@RequestMapping("/medicaltech/lisQc")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:lisQc:list')")
public class LisQcController {

    private final LisQcService lisQcService;

    /**
     * 质控计划分页。
     * 用于室内质控页「质控计划」标签的查询表格。
     */
    @Operation(summary = "质控计划分页")
    @PostMapping("/planListPage")
    public Result<PageResult<LisQcVO.PlanVO>> planListPage(@Valid @RequestBody LisQcDTO.PlanQuery query) {
        return Result.success(lisQcService.planPage(query));
    }

    /**
     * 新增或修改质控计划（检验项目 × 仪器 × 质控品的靶值、SD 与效期）。
     * 用于室内质控页质控计划的新增与修改。
     */
    @PreAuthorize("hasAuthority('medtech:lisQc:add')")
    @Operation(summary = "质控计划新增/修改")
    @PostMapping("/planUpsert")
    public Result<String> planUpsert(@Valid @RequestBody LisQcDTO.PlanUpsert dto) {
        String planNo = lisQcService.planUpsert(dto);
        return Result.success("质控计划 " + planNo + " 已保存", planNo);
    }

    /**
     * 启用或停用一条质控计划。
     * 用于室内质控页质控计划行的状态开关。
     */
    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "质控计划启用/停用")
    @PostMapping("/planToggle")
    public Result<Void> planToggle(@Valid @RequestBody LisQcDTO.PlanToggle dto) {
        lisQcService.planToggle(dto.getPlanId(), dto.getStatus());
        return Result.success(dto.getStatus() == 1 ? "计划已启用" : "计划已停用", null);
    }

    /**
     * 录入质控测定值，由服务端按 Westgard 规则判定在控或失控。
     * 用于室内质控页每日质控的结果登记。
     */
    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "录入质控结果（Westgard 服务端判定）")
    @PostMapping("/inputResult")
    public Result<LisQcVO.RecordVO> inputResult(@Valid @RequestBody LisQcDTO.ResultInput dto) {
        return Result.success("质控结果已录入并判定", lisQcService.inputResult(dto));
    }

    /**
     * 质控记录分页。
     * 用于室内质控页「质控记录」标签的查询表格。
     */
    @Operation(summary = "质控记录分页")
    @PostMapping("/recordListPage")
    public Result<PageResult<LisQcVO.RecordVO>> recordListPage(@Valid @RequestBody LisQcDTO.RecordQuery query) {
        return Result.success(lisQcService.recordPage(query));
    }

    /**
     * 登记失控处理：填写失控原因与纠正措施，处理后进入待复核。
     * 用于室内质控页失控记录的「处理」动作。
     */
    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "失控处理（原因 + 纠正措施）")
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody LisQcDTO.Handle dto) {
        lisQcService.handle(dto);
        return Result.success("失控已处理，待复核", null);
    }

    /**
     * 复核失控处理，复核人不得是处理人本人，通过后失控闭环。
     * 用于室内质控页已处理失控记录的「复核」动作。
     */
    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "失控复核（复核人不得是处理人本人）")
    @PostMapping("/review")
    public Result<Void> review(@Valid @RequestBody LisQcDTO.Review dto) {
        lisQcService.review(dto);
        return Result.success("复核通过，失控闭环完成", null);
    }

    /**
     * 质控概览统计：今日在控率与待处理失控数。
     * 用于室内质控页顶部的统计卡。
     */
    @Operation(summary = "质控统计（今日在控率 / 待处理失控数）")
    @GetMapping("/stats")
    public Result<LisQcVO.StatsVO> stats() {
        return Result.success(lisQcService.stats());
    }
}
