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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * LIS 室内质控接口（URL 前缀 /medicaltech/lisQc）
 *
 * <p>判定结果（status / violatedRules / zScore）全部服务端算好落库，前端只读。
 */
@Tag(name = "LIS室内质控")
@RestController
@RequestMapping("/medicaltech/lisQc")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:lisQc:list')")
public class LisQcController {

    private final LisQcService lisQcService;

    @Operation(summary = "质控计划分页")
    @PostMapping("/planListPage")
    public Result<PageResult<LisQcVO.PlanVO>> planListPage(@RequestBody LisQcDTO.PlanQuery query) {
        return Result.success(lisQcService.planPage(query));
    }

    @PreAuthorize("hasAuthority('medtech:lisQc:add')")
    @Operation(summary = "质控计划新增/修改")
    @PostMapping("/planUpsert")
    public Result<String> planUpsert(@Valid @RequestBody LisQcDTO.PlanUpsert dto) {
        String planNo = lisQcService.planUpsert(dto);
        return Result.success("质控计划 " + planNo + " 已保存", planNo);
    }

    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "质控计划启用/停用")
    @PostMapping("/planToggle")
    public Result<Void> planToggle(@Valid @RequestBody LisQcDTO.PlanToggle dto) {
        lisQcService.planToggle(dto.getPlanId(), dto.getStatus());
        return Result.success(dto.getStatus() == 1 ? "计划已启用" : "计划已停用", null);
    }

    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "录入质控结果（Westgard 服务端判定）")
    @PostMapping("/inputResult")
    public Result<LisQcVO.RecordVO> inputResult(@Valid @RequestBody LisQcDTO.ResultInput dto) {
        return Result.success("质控结果已录入并判定", lisQcService.inputResult(dto));
    }

    @Operation(summary = "质控记录分页")
    @PostMapping("/recordListPage")
    public Result<PageResult<LisQcVO.RecordVO>> recordListPage(@RequestBody LisQcDTO.RecordQuery query) {
        return Result.success(lisQcService.recordPage(query));
    }

    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "失控处理（原因 + 纠正措施）")
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody LisQcDTO.Handle dto) {
        lisQcService.handle(dto);
        return Result.success("失控已处理，待复核", null);
    }

    @PreAuthorize("hasAuthority('medtech:lisQc:edit')")
    @Operation(summary = "失控复核（复核人不得是处理人本人）")
    @PostMapping("/review")
    public Result<Void> review(@Valid @RequestBody LisQcDTO.Review dto) {
        lisQcService.review(dto);
        return Result.success("复核通过，失控闭环完成", null);
    }

    @Operation(summary = "质控统计（今日在控率 / 待处理失控数）")
    @GetMapping("/stats")
    public Result<LisQcVO.StatsVO> stats() {
        return Result.success(lisQcService.stats());
    }
}
